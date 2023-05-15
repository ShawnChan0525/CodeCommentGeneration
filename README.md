# iCommenter

## gradlePlugin

系统客户端

## Django_Server

系统服务器端。创建服务器端虚拟环境有如下步骤：
1. 创建虚拟环境
`python -m venv djangoenv`
2. 激活虚拟环境
`djangoenv\Scripts\activate`
3. 安装Django模块
`pip install Django`
4. 使用django-admin创建一个叫cs_tool的项目
`django-admin startproject cs_tool`
或`python -m django startproject cs_tool`
5. 创建iCommenter应用
`django-admin startapp iCommenter`
本仓库Django_Server文件夹中存放的即为iCommenter的核心代码
6. 启动后端服务
` python manage.py runserver 8080`

## ECALE

系统算法端