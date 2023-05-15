# iCommenter

## gradlePlugin

系统客户端

## Django_Server

系统服务器端。服务器端经如下步骤搭建与运行：
1. 创建虚拟环境<br>
`python -m venv djangoenv`

2. 激活虚拟环境<br>
`djangoenv\Scripts\activate`

3. 安装Django模块<br>
`pip install Django`

4. 使用django-admin创建cs_tool项目<br>
`django-admin startproject cs_tool`<br>
或`python -m django startproject cs_tool`

5. 创建iCommenter应用，本仓库Django_Server文件夹中存放的即为iCommenter的核心代码<br>
`django-admin startapp iCommenter`

6. 启动后端服务<br>
` python manage.py runserver 8080`

## ECALE

系统算法端