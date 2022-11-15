<head>
<title>Countdown solver</title>
<meta name="viewport" content="width=100, initial-scale=1">
</head>
<body>
<form action="/" method="put">
Target: <input name="num0" type="number" style="width: 12%" value="${(input![])[0]!""}"/>
<p>
Source numbers:
<br>
<#list 1..6 as n>
<input name="num${n}" type="number" style="width: 12%" value="${(input![])[n]!""}"/>
</#list>
<p><input type="submit" value="OK"/>
</form>
<b>
<#list messages as msg>
<p>${msg}</p>
</#list>
</body>