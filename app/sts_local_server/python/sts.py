#!/usr/bin/env python
#coding=utf-8
#下载sts授权库 pip install aliyun-python-sdk-sts
from aliyunsdkcore import client
from aliyunsdksts.request.v20150401 import AssumeRoleRequest

def getSts():

	# 通过管理控制后台-访问控制 https://help.aliyun.com/product/28625.html?spm=5176.doc28649.3.1.gXMMoS
	# RAM用户创建步骤
	# 1.创建RAM用户 https://help.aliyun.com/document_detail/28637.html?spm=5176.product28625.4.1.loOt1V
	# 2.创建自定义授权策略 https://help.aliyun.com/document_detail/28640.html?spm=5176.product28625.4.2.QCYMTW
	# 3.给RAM用户授权 https://help.aliyun.com/document_detail/28639.html?spm=5176.product28625.4.3.4ji7k1
	# 4.RAM用户登录控制台 https://help.aliyun.com/document_detail/43640.html?spm=5176.product28625.4.4.0dU4bF
	# 只有 RAM用户（子账号）才能调用 AssumeRole 接口
	# 阿里云主账号的AccessKeys不能用于发起AssumeRole请求
	# 请首先在RAM控制台创建一个RAM用户，并为这个用户创建AccessKeys
	# 对子账户需要设置AliyunSTSAssumeRoleAccess权限
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

	#OSS Policy settings
	#can read https://help.aliyun.com/document_detail/56288.html
	#case https://help.aliyun.com/knowledge_detail/39717.html?spm=5176.product28625.6.735.5etPTf
	#case https://help.aliyun.com/knowledge_detail/39712.html?spm=5176.7739717.6.729.aZiRgD
	policy = '{"Version":"1","Statement":[{"Action":["oss:*"],"Resource":["acs:oss:*:*:*"],"Effect":"Allow"}]}'

	request.set_Policy(policy)
	# 发起请求，并得到response
	response = clt.do_action_with_exception(request)

    print response
	return response
	pass