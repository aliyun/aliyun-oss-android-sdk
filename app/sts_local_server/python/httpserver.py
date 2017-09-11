#!/usr/bin/env python  
# encoding: utf-8  
# 使用步骤：
# 1.如果没有python 请安装 python 2.7 https://www.python.org/
# 2.安装python模块管理工具pip mac环境sudo easy_install pip windows 请自行网上搜索
# 3.下载sts授权库 pip install aliyun-python-sdk-sts
# 4.安装web模块 pip install web.py 如果是mac没权限请加sudo，已有请忽略
# 5.python本机启动一个http服务  cd到文件所在目录 敲入指令 -- [python httpserver.py local_ip:port(>3000)] 
import web
import sts
  
urls = (  
        '/sts/getsts', 'getsts'   # 第一个参数是访问路径  第二个参数是具体的接口
        )  
app = web.application(urls, globals())  
  
  
class getsts:  
    def POST(self):  
       return sts.getSts()
    def GET(self):
        return sts.getSts()
  
if __name__ == "__main__":  
    app.run() 