<%--
 * $RCSfileUploadServlet.java,v $
 * version $Revision$
 * created 25.06.2018 13:09 by elvira
 * last modified $Date$ by $Author$
 * <br>
 * Copyright 2004-2018 Crypto-Pro. All rights reserved.
 * <br>
 * Программный код, содержащийся в этом файле, предназначен
 * для целей обучения. Может быть скопирован или модифицирован
 * при условии сохранения абзацев с указанием авторства и прав.
 *
 * Данный код не может быть непосредственно использован
 * для защиты информации. Компания Крипто-Про не несет никакой
 * ответственности за функционирование этого кода.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%
  String  signatureValue = request.getAttribute("SigInBase")!=null ? (String)request.getAttribute("SigInBase") : "";
  String resultMsg = request.getAttribute("Result")!=null ? (String)request.getAttribute("Result") : "";
  String logMsg = request.getAttribute("Log")!=null ? (String)request.getAttribute("Log") : "";
%>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html" charset="UTF-8">
  <title>Проверка подписи</title>
</head>
<body>
<form action="verify" method="post" enctype="multipart/form-data">
    <table>
    <tr>
        <td width="800" valign="top">
            <h1 align="center">Проверка подписи</h1>
            <p></p>
            <p><b>Выберите тип подписи: </b></p>
            <p><select size="4" name="SigTypeBox" id="SigTypeBox" style="width:600px;resize:none;border-style:solid;">
            <option selected value="Default">Default (for CAdES)</option>
            <option value="PKCS7">PKCS7</option>
            <option value="CAdES-BES">CAdES-BES</option>
            <option value="CAdES-T">CAdES-T</option>
            <option value="CAdES-XLT1">CAdES-X Long Type 1</option>
            <option value="XAdES-BES">XAdES-BES</option>
            <option value="XAdES-T">XAdES-T</option>
            <option value="XAdES-XLT1">XAdES-X Long Type 1</option>
            <option value="SignPDF">Sign PDF</option>
            </select></p>
            <p><b>Выберите файл с подписью:</b></p>
            <p><input type="file" id="SigFile" name="SigFile" style="width:600px;resize:none;border-style:solid;border-width:thin;"></p>
            <p><b>либо введите подпись в кодировке BASE64 ниже: </b></p>
            <p><textarea id="SigInBase" name="SigInBase" style="height:400px;width:600px;resize:none;border-style:solid;"><%=signatureValue%></textarea></p>
            <p></p>
            <p><b>Подпись отделенная (для CAdES)</b> <input type="checkbox" name="isDetached"></p>
            <p></p>
            <p><b>Выберите файл с данными (только для отделенной CAdES подписи):</b></p>
            <p><input type="file" id="DataFile" name="DataFile" style="width:600px;resize:none;border-style:solid;border-width:thin;"></p>
            <p></p>
            <p><b>Выберите сертификат(для подписи CAdES-BES):</b></p>
            <p><input type="file" id="CertFile" name="CertFile" style="width:600px;resize:none;border-style:solid;border-width:thin;"></p>
            <p></p>
            <p></p>
            <p><input type="submit" id="verify" name="verify" style="height:40px; width:300px;" value="Проверить подпись">&nbsp;&nbsp;&nbsp;
                <input type="button" id="cancel" name="cancel" style="height:40px; width:300px;" value="Очистить форму" onclick="document.getElementById('SigInBase').value='';
            document.getElementById('SigFile').value='';document.getElementById('DataFile').value='';document.getElementById('CertFile').value='';
            document.getElementById('Result').value='';"></p>
        </td>
        <td width="600" valign="top">
            <p></p>
            <p></p>
            <fieldset>
                <legend><h3>Результат проверки:</h3></legend>
                <p><textarea id="Result" name="Result" readonly  style="height:400px;width:600px;resize:none;border-style:none;"><%=resultMsg%></textarea></p>
            </fieldset>
        </td>
    </tr>
    </table>
</form>
<p></p>
<p></p>
<h3>Лог выполнения</h3>
<%=logMsg%>
</body>
</html>