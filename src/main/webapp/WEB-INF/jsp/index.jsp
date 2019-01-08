<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
    <%
        String rootPath = request.getContextPath();
        request.setAttribute("rootPath", rootPath);
    %>
    <title>微博接口数据查询</title>
</head>
<body class="bootstrap-admin-without-padding">
<div class="container">
    <div class="row">
        <form method="post" action="${rootPath}/statuses/limited">
            <a href="https://open.weibo.com/wiki/C/2/search/statuses/limited">微博博文返回格式</a><br>
            <p>关键词内容支持与、或、非。最好是5个词以内，并且最好一次使用一种语法；<br>
            与：空格即可，如A B<br>
            或：~表示，如A~B~C<br>
            非：空格-，比如A -B，表示出现A,不出现B<br>
                混用情况：(A B)~(C D)表示同时出现A,B或同时出现C,D， A B -C表示同时出现A,B但不出现C</p>
            <input type="text" name="q" placeholder="关键词">
            <p>开始时间和结束时间格式为yyyy-MM-dd，例如2018-01-01</p>
            <input type="text" name="starttime" placeholder="开始时间">
            <input type="text" name="endtime" placeholder="结束时间">
            <p>排序方式，time：时间倒序、hot：热门度【设置此参数只会返回精选微博】、fwnum：按转发数倒序、cmtnum：评论数倒序，默认为time。</p>
            <select name="sort">
                <option value="time">time</option>
                <option value="hot">hot</option>
                <option value="fwnum">fwnum</option>
                <option value="cmtnum">cmtnum</option>
            </select>
            <p>页码，默认为1</p>
            <input type="text" name="page" placeholder="页码" value="1">
            <p>每页返回的数量，最小为10，最大50。（默认返回50条）</p>
            <input type="text" name="count" placeholder="每页返回的数量" value="50">
            <button type="submit">提交</button>
        </form>
    </div>
</div>
</body>
</html>
