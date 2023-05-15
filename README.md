# iCommenter

## gradlePlugin

系统客户端。客户端经如下步骤搭建与运行：
1. 在**设置-项目结构**里配置正确的SDK版本：Intellij平台插件SDK，选择内部java平台为11的版本

2. 点击**gradlePlugin[ runIde ]**按钮，等待gradlde构建，并运行沙盒演示，最终将展示Pycharm的初始界面

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

系统算法端。
<br>
主要实现了基于对齐学习的代码注释生成技术ECALE（Enhancing Code-Comment Alignment Learning for Code Comment Generation）。主要分为**数据预处理模块**、**对齐学习训练模块**、**代码注释生成模型训练模块**三个部分。

### 环境与依赖
ubuntu 18.04<br>
python==3.8<br>
torch==1.7.1<br>
transformers==4.6.1<br>
tqdm==4.64.0<br>
numpy==1.22.3

### 数据集
- JCSD和PCSD：https://github.com/gingasan/sit3
- CodeSearchNet：https://github.com/github/CodeSearchNet

### ECALE实现
#### 数据预处理模块
`python utils/split.py --dataset_name JCSD --aw_cls 40`

#### 对齐学习训练模块
`python encoder_finetune.py --output_dir outputdir/ECALE --dataset_name JCSD --model_name_or_path  microsoft/unixcoder-base --with_test --with_mlm --with_ulm --with_awp --with_cuda --epochs 50`

#### 代码注释生成模型训练模块
`python decoder_finetune.py --output_dir outputdir/ECALE --dataset_name JCSD --model_name_or_path  microsoft/unixcoder-base --unified_encoder_path outputdir/ECALE/unified_encoder_model/model.pth  --do_train --do_eval --do_pred --with_cuda --eval_steps 5000 --train_steps 100000`