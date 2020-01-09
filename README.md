- **功能1**，格式转换，更新文件名
  - dxf -> svg 
    - svg：xml格式的矢量图，根据svg代码可提取出部件名信息
  - dxf -> pdf -> jpg
    - pdf足够清晰，可调整生成jpg的清晰度

**svg文件结构**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" viewBox="SVG绘图区域(min x, min y, width, height)" version="1.0" overflow="visible">  
    <defs/>  
<g>图像本体代码</g>
</svg>

```
- **功能2**，dxf图像切割
  - dxf -> svg -> svgs
  - 依照svg文件代码结构，将包含多个子图的dxf切割
    1. 观察代码结构，想尽办法分出每个子图的本体代码
    2. 将子图本体代码用`` <g></g> ``包裹，并加上``<?xml>、<svg>``等必须代码
    3. 想尽办法计算出viewBox并修改svg标签对应属性
***
全在pakage dxfFileProcess 中，后期再打包为exe等  
待添加功能：
- 上传文件路径选择（目前写死为工程目录）
- svg -> jpg清晰度（目前写死）
***
环境：  
- jre 1.8
- batik 1.10
- kabeja 0.4
  - 解析dxf文件，并可转化为svg、pdf
- padbox 2.0.1
  - pdf -> jpg

---
**Main.java**  ：转换+分割
1. 读取：工程路径/images/
2. 转换实例dxf：/resources/compPic.dxf 为svg
3. 分割此svg
4. svg -> jpg

## public接口说明：

**DXFConver.java**：dxf->svg
- GetSourceFile(String filePath)
  - 通过filePath路径获取文件，并**转化为DXFDocument**
- DXF2SVG(DXFDocument doc, String outFilePath) 
  - DXF -> SVG(XML)
- ChangeNameToPieceName(String filePath)
  -  通过filePath路径获取svg文件
  -  读取svg文件中部件名(piece name)并更改文件名
- DXF2PDF(DXFDocument doc, String fileName)
  - dxf -> pdf

**SVGProcess.java**：封装对SVG的操作，封装SVGConvert.java
> 封装了根据代码结构切割SVG、查找到代码中的部件名并重命名，计算并重制viewBox使画面正中且完整，补全切割出的代码为完整的SVG文件。
- SVGDevide(String filePath, String outFilePath)
- SVG2JPG() 与SVGDevide共用路径

**SVGConvert.java**：svg->pdf->jpg
- SVGs2JPGs(String filePath, List<String> nameList)，转换并重命名。
  - pdf放filePath/padImages/中
  - jpg放filePath/jpgImages/中
- SVG2JPG(String filePath, String outFilePath, float resolution) 单张图片的转换，可设置清晰度
  - resolution=25.0f时约1M