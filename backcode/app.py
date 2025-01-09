# app.py

from io import BytesIO

from flask import Flask, request, send_file, jsonify
from PIL import Image
import numpy as np
from flask_cors import CORS


from utils import (
    encryption,
    decrypted,
    embed,
    extract,
    DtoB
)

app = Flask(__name__)
CORS(app, expose_headers=["X-Embedded-Data"])


# ========== 配置区域 ==========

# 可以把密钥放到配置或环境变量中，这里直接写死做示例
# str1 = '54659883'
# 这里要注意：RC4.new(key) 期望的是字节类型
ENCRYPT_KEY = b"54659883"   # bytes 类型

EMBED_KEY = "5555"          # 用于 embed/extract 的 key，字符串即可
embed_mes = "76543210"
embed_data = DtoB(embed_mes)
latest_encrypted_image = None
# ========== 接口示例 ==========


@app.route("/encrypt_image", methods=["POST"])
def encrypt_image():
    try:
        global latest_encrypted_image

        if "file" not in request.files:
            return jsonify({"error": "No file part"}), 400

        file = request.files["file"]
        if file.filename == "":
            return jsonify({"error": "No selected file"}), 400

        image = Image.open(file.stream).convert('L')
        pixels = np.array(image)

    # 假设已有 encryption 函数

        e_image = encryption(pixels, ENCRYPT_KEY)
        e_image = embed(e_image, embed_data, EMBED_KEY, 32)
    # 存到全局变量
        latest_encrypted_image = e_image
        # 在这儿增加一条调试信息，打印数组的形状/数据类型或其他你想看的信息
        print("[DEBUG] latest_encrypted_image shape:",
              latest_encrypted_image.shape)
        print("[DEBUG] latest_encrypted_image dtype:",
              latest_encrypted_image.dtype)
    except Exception as e:
        return jsonify({'status': 'error', 'message': f'Processing error: {e}'}), 500
    # 只返回"接收成功"的状态
    return jsonify({'status': 'success', 'message': 'Files uploaded and processed successfully'}), 200


@app.route("/get_latest_image", methods=["GET"])
def get_latest_image():
    global latest_encrypted_image
    print("[DEBUG] latest_encrypted_image shape:",
          latest_encrypted_image.shape)
    print("[DEBUG] latest_encrypted_image dtype:",
          latest_encrypted_image.dtype)
    if latest_encrypted_image is None:
        return jsonify({"error": "No encrypted image available"}), 404

    # 转为 PIL、送回
    pil_encrypted = Image.fromarray(latest_encrypted_image, mode='L')
    img_io = BytesIO()
    pil_encrypted.save(img_io, format='PNG')
    img_io.seek(0)

    return send_file(img_io, mimetype='image/png')


@app.route("/decrypt_latest_image", methods=["GET"])
def decrypt_latest_image():
    global latest_encrypted_image
    if latest_encrypted_image is None:
        return jsonify({"error": "No latest encrypted image available"}), 404

    e_image = latest_encrypted_image
    dec_image = decrypted(e_image, ENCRYPT_KEY)

    # 提取嵌入数据
    data_bits, recover_image = extract(dec_image, EMBED_KEY, 32)
    #data_str = "".join(str(bit) for bit in data_bits)
    print("data_bits",
          data_bits)

    # 如果你想返回“修复后图像”而不是纯解密图像，可用 recover_image
    pil_decrypted = Image.fromarray(recover_image, mode='L')

    from io import BytesIO
    img_io = BytesIO()
    pil_decrypted.save(img_io, format='PNG')
    img_io.seek(0)

    # 构造返回
    response = send_file(img_io, mimetype='image/png')
    # 在响应头里添加自定义的“X-Embedded-Data”
    response.headers["X-Embedded-Data"] = data_bits
    return response



# ========== 调试日志增强 ==========
@app.before_request
def log_request_info():
    """
    打印每次请求的调试信息
    """
    print("[REQUEST] Method:", request.method)
    print("[REQUEST] URL:", request.url)
    print("[REQUEST] Headers:", request.headers)


@app.after_request
def log_response_info(response):
    """
    打印每次响应的调试信息
    """
    print("[RESPONSE] Status:", response.status)
    print("[RESPONSE] Headers:", response.headers)
    return response


if __name__ == "__main__":
    # 在开发环境直接运行
    # 线上环境可以用 gunicorn 或 uwsgi 来部署
    # 例如 gunicorn -w 4 -b 0.0.0.0:5000 app:app
    app.run(host="0.0.0.0", port=5000, debug=True)



