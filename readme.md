url中的用户id分为两种
(1)http://weibo.com/cctvxinwen
(2)http://weibo.com/2656274875
这两个url都代表的是"央视新闻"微博账号,
所有类中,id代表昵称,oid代表第二种,即"2656274875"


抓取数量有限制，测试了下，大约发送1200条请求就不能继续了，然后被禁止访问接近3.5小时
程序中限制每次只能最多发送1000条请求，如果配置的抓取任务超过1000条就不回进行抓取工作