package dxfFileProcess;

import java.io.FileNotFoundException;

import org.kabeja.dxf.DXFDocument;
import org.xml.sax.SAXException;

/*
 * 1、DXF -> SVG (Kabeja)
 * 2、SVG -> svgs 
 * 			居中处理：viewBox="各点"看到"这个SVG绘图区域。由空格或逗号分隔的4个值。(min x, min y, width, height)"
 * 				设置ViewBox，(最小x,  |最大y|， 最大x-最小x，最大y-最小y)
 * 			读取xml中记录的Piece Name
 * 			(以上处理利用DOM4J)
 * 3、svg -> pdf (batik),  pdf -> jpg (pdfbox)
 */
public class Main {

	public static void main(String[] args) {
		// 获取工程根目录/image
		String imagePath = System.getProperties().getProperty("user.dir")+"/images";
		// 1、DXF -> SVG（路径：工程路径/images/）
		DXFDocument dxfDoc = DXFConvert.GetSourceFile(imagePath+"/resources/compPic.dxf");
		try {
			DXFConvert.DXF2SVG(dxfDoc, imagePath+"/resources/compPic.svg");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		// 2、SVG -> svgs
		SVGProcess.SVGDevide(imagePath+"/resources/compPic.svg", imagePath);
		// 3、svgs -> jpgs
		SVGProcess.SVG2JPG();
		System.out.println("转换完成！");
	}

}
