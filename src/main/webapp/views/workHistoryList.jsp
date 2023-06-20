<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>근무 히스토리</title>

  <!-- Google Font: Source Sans Pro -->
  <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Source+Sans+Pro:300,400,400i,700&display=fallback">
  <!-- Font Awesome -->
  <link rel="stylesheet" href="../../plugins/fontawesome-free/css/all.min.css">
  <!-- Theme style -->
  <link rel="stylesheet" href="../../dist/css/adminlte.min.css">
</head>
<body>
<jsp:include page = "index.jsp"></jsp:include>
<!-- Site wrapper -->
<div class="wrapper">
  <div class="content-wrapper">
    <section class="content-header">
            <h1>근무 히스토리</h1>         
    </section>
    <br/>
    <!-- Main content -->
    <section class="content">    
    	<table class="table table-bordered">
    		<thead>
    			<tr>
    				<td>번호</td>
    				<td>아이디</td>
    				<td>이름</td>
    				<td>날짜</td>
    				<td>출근 시간</td>
    				<td>퇴근 시간</td>
    				<td>근무 시간</td>    				
    				<td>수정</td>
    			</tr>    		
    		</thead>
    		<tbody>    		
	    		<c:forEach items="${list}" var="member">
					<tr>
	    				<td>${member.id}</td>
	    				<td>${member.member_id}</td>
	    				<td>${member.name}</td>
	    				<td>${member.date}</td>
	    				<td>${member.time_go}</td>
	    				<td>${member.time_end}</td>    				
	    				<td>${member.time_go}-${member.time_end}</td>
	    				<td>${member.date}</td>
	    				<td><a href="WorkChangeRequest.do?id=${member.id}">수정</a></td>
	    			</tr>			
				</c:forEach>
    		</tbody>    	
    	</table>
    </section>
  </div>
</div>
<!-- ./wrapper -->






<!-- jQuery -->
<script src="../../plugins/jquery/jquery.min.js"></script>
<!-- Bootstrap 4 -->
<script src="../../plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
<!-- AdminLTE App -->
<script src="../../dist/js/adminlte.min.js"></script>
<!-- AdminLTE for demo purposes -->
<script src="../../dist/js/demo.js"></script>
</body>
<script>
</script>
</html>
