#!/usr/bin/env python  
# encoding: utf-8  
# python本机启动一个http服务  cd到文件所在目录 敲入指令 --  python httpserver.py 本机ip:port
import web   #如果未安装 请 pip install web.py 如果是mac没权限请加sudo 如没有pip请自行搜索安装
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