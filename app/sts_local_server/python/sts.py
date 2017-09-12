#!/usr/bin/env python
#coding=utf-8
#下载sts授权库 pip install aliyun-python-sdk-sts
from aliyunsdkcore import client
from aliyunsdksts.request.v20150401 import AssumeRoleRequest

def getSts():

	# 通过管理控制后台-访问控制 https://help.aliyun.com/product/28625.html
	# 子用户(User)创建步骤
	# 1.创建子用户(User) https://help.aliyun.com/document_detail/28637.html
	# 2.创建自定义授权策略 https://help.aliyun.com/document_detail/28640.html
	# 3.给子用户(User)授权 https://help.aliyun.com/document_detail/28639.html
	# 4.子用户(User)登录控制台 https://help.aliyun.com/document_detail/43640.html
	# 只有子用户(User)才能调用 AssumeRole 接口
	# 阿里云主用户(Root User)的AccessKeys不能用于发起AssumeRole请求
	# 请首先在RAM控制台创建一个子用户(User)，并为这个用户创建AccessKeys
    # 创建 User/Role可以在主用户的控制台上一起完成的
	# 对子用户(User)需要设置AliyunSTSAssumeRoleAccess权限
	# 构建一个 Aliyun Client, 用于发起请求
	# 构建Aliyun Client时需要设置AccessKeyId和AccessKeySevcret
	# STS是Global Service, API入口位于华东 1 (杭州) , 这里Region填写"cn-hangzhou"
	# clt = client.AcsClient('<access-key-id>','<access-key-secret>','cn-hangzhou')
	ak = "***************************"
	sk = "***************************"
	role = "***************************"

	clt = client.AcsClient(ak,sk,'cn-hangzhou')
	# 构造"AssumeRole"请求
	request = AssumeRoleRequest.AssumeRoleRequest()
	# 指定角色 需要在 RAM 控制台上获取
	request.set_RoleArn(role)
	# RoleSessionName 是临时Token的会话名称，自己指定用于标识你的用户，主要用于审计，或者用于区分Token颁发给谁
	# 但是注意RoleSessionName的长度和规则，不要有空格，只能有'-' '.' '@' 字母和数字等字符
	# 具体规则请参考API文档中的格式要求
	request.set_RoleSessionName('al001')

	#OSS Policy settings  could not set by default
	#can read https://help.aliyun.com/document_detail/56288.html
	#case https://help.aliyun.com/knowledge_detail/39717.html?spm=5176.product28625.6.735.5etPTf
	#case https://help.aliyun.com/knowledge_detail/39712.html?spm=5176.7739717.6.729.aZiRgD
    
	# 发起请求，并得到response
	response = clt.do_action_with_exception(request)

    print response
	return response
	pass
