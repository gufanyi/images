<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div layoutH="0">
    <c:forEach items="${groups}" var="group" varStatus="status">
        <div class="panel" >
            <h1>FASTDFS组:${group.groupName}</h1>
            <c:forEach items="${group.storageList}" var="storageFile">
                <div class="storageForColumn">
                    <input type="hidden" value="${storageFile.ipAddr}" name="ipStorageForTen">
                    <input type="hidden" value="${storageFile.totalDownloadCount}" name="totalDownloadCountByIp"/>
                    <table>
                        <tr>
                            <td>
                                <div class="chart">
                                </div>
                            </td>
                            <td>
                                <div class="pieCharts"></div>
                            </td>
                        </tr>
                    </table>


                </div>
            </c:forEach>
        </div>
    </c:forEach>
</div>
<script type="text/javascript">
    function drawColumnForTenFile(id, ip, x,y, total) {
        new Highcharts.Chart({
            chart:{
                renderTo:id,
                height:240,
                width:800,
                type:'column'
            },
            title:{
                text:ip + '服务器被访问最多的前十个文件',

                style:{
                    fontSize:'13px'
                }
            },
            subtitle:{
                text:"本服务器一共被访问文件:" + total,
                style:{
                    fontSize:'12px'
                }
            },
            plotOptions:{
                column:{

                    dataLabels:{
                        color:'red',
                        enabled:true,
                        style:{
                            fontWeight:'bold'

                        },
                        formatter:function () {
                            return this.y + '次';
                        }
                    }

                }
            },
            xAxis:{

                 categories:x

            },
            yAxis:{
                title:false,
                plotLines:[
                    {

                        width:10,
                        color:'#808080'

                    }
                ]
            },
            legend:{
                enabled:false
            },
//            tooltip: {  // 表示为 鼠标放在报表图中数据点上显示的数据信息
//                formatter: function() {
//                    return '<b>'+'日期:' +'</b>'
//                            +Highcharts.dateFormat('%Y-%m-%d %H:%M', this.x) +'<br/>'
//                            + this.series.name + '</b>'+ ': ' + this.y + '%';
//                }
//            },
//            legend : {
//                layout : 'vertical',
//                align : 'left',
//                floating: true,
//                verticalAlign : 'top',
//                x : 100,
//                y : 4,
//                borderWidth : 1
//            },
            series:y
        });
    }
    function drawPieForAllFile(id,data,ip){
        new Highcharts.Chart({
            chart:{
                renderTo:id,
                height:240,
                width:300,
                type:'pie'
            },
            title:{
                text:ip + '文件访问前10名分布图',

                style:{
                    fontSize:'13px'
                }
            },
            xAxis:{



            },
            yAxis:{
                plotLines:[
                    {

                        width:10,
                        color:'#808080'

                    }
                ]
            },
//            tooltip: {  // 表示为 鼠标放在报表图中数据点上显示的数据信息
//                formatter: function() {
//                    return '<b>'+ this.point.name +'</b> '/;
//                }
//            },
//            },
//            legend : {
//                layout : 'vertical',
//                align : 'left',
//                floating: true,
//                verticalAlign : 'top',
//                x : 100,
//                y : 4,
//                borderWidth : 1
//            },
            series:data
        });
    }
    $("div.storageForColumn").each(function () {
        var toRender = $(this);
        var ip = toRender.find('input[name=ipStorageForTen]').val();
        var total = toRender.find('input[name=totalDownloadCountByIp]').val();
        $.getJSON('testModule/tenFileDownLoad.shtml', {ip:ip}, function (data) {
            var x=data.x;
            var y=data.y;
            drawColumnForTenFile(toRender.find('.chart')[0], ip, x,y, total);
        });
        $.getJSON('testModule/allFilePie.shtml',{ip:ip},function(data){
            drawPieForAllFile(toRender.find('.pieCharts')[0],data,ip);
        })
    });
</script>