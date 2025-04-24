// 本地文件服务器，用于开发环境下访问本地图片
const express = require('express');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = 3000;

// 启用CORS，允许前端应用访问
app.use(cors());

// 静态文件服务，指向Nginx的文件目录
app.use('/hmdp', express.static('C:/code/different_code/front-end/hmdp-nginx-1.18.0/html/hmdp'));

app.listen(PORT, () => {
  console.log(`本地文件服务器运行在 http://localhost:${PORT}`);
  console.log(`您可以通过 http://localhost:${PORT}/hmdp/imgs/... 访问图片`);
}); 