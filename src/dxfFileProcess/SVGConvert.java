package dxfFileProcess;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class SVGConvert {
	/**
	 * @Title: SVG2PDF
	 * @Description: TODO SVG -> PDF -> JPG������
	 * @param filePath ����svg�ļ�·��x:/xxx (�����ͬһ�ļ���)
	 * @return: void
	 * @throws IOException
	 * @throws TranscoderException
	 */
	public static void SVGs2JPGs(String filePath, List<String> nameList) throws IOException, TranscoderException {
		Transcoder transcoder = new PDFTranscoder();
		for (int i = 0; i < nameList.size(); i++) {
			String svgPath = filePath + "/";
			String pdfPath = filePath + "/pdfImages/";
			String jpgPath = filePath + "/jpgImages/";
			// ����·���ļ���
			CreateFilePath(pdfPath);
//			System.out.println("pdfPath:" + pdfPath);
			CreateFilePath(jpgPath);
			// svg -> pdf
			TranscoderInput transcoderInput = new TranscoderInput(
					new FileInputStream(svgPath + nameList.get(i) + ".svg"));
			TranscoderOutput transcoderOutput = new TranscoderOutput(
					new FileOutputStream(pdfPath + nameList.get(i) + ".pdf"));
			transcoder.transcode(transcoderInput, transcoderOutput);
			// pdf -> jpg
			PDDocument pdfDoc = PDDocument.load(new File(pdfPath + nameList.get(i) + ".pdf"));
			PDFRenderer renderer = new PDFRenderer(pdfDoc);
			// �ڶ�������Խ������ͼƬ�ֱ���Խ�ߣ�ת��ʱ��Ҳ��Խ����25Լ1M
			BufferedImage image = renderer.renderImage(0, 25.0f); 
			ImageIO.write(image, "JPG", new File(jpgPath + nameList.get(i) + ".jpg"));
			System.out.println("File " + (i + 1) + " SVG to JPG Done");
			pdfDoc.close();
		}
	}

	private static void CreateFilePath(String path) {
		File f = new File(path);
		if (!f.exists())
			f.mkdir();
	}

	/**
	 * @Title: SVG2JPG   
	 * @Description: TODO   ����SVG -> JPG
	 * @param filePath svg�ļ�·��
	 * @param outFilePath ���jpg�ļ�·��
	 * @param resolution ���jpg�����ȣ�25.0fԼ1M��
	 * @throws IOException
	 * @throws TranscoderException      
	 * @return: void
	 */
	public static void SVG2JPG(String filePath, String outFilePath, float resolution) throws IOException, TranscoderException {
		Transcoder transcoder = new PDFTranscoder();
		String pdfFilePath = outFilePath.substring(0, outFilePath.lastIndexOf('.')) + ".pdf";
		// svg -> pdf
		TranscoderInput transcoderInput = new TranscoderInput(new FileInputStream(filePath));
		TranscoderOutput transcoderOutput = new TranscoderOutput(
				new FileOutputStream(pdfFilePath));
		transcoder.transcode(transcoderInput, transcoderOutput);
		// pdf -> jpg
		PDDocument pdfDoc = PDDocument.load(new File(pdfFilePath));
		PDFRenderer renderer = new PDFRenderer(pdfDoc);
		BufferedImage image = renderer.renderImage(0, resolution); // �ڶ�������Խ������ͼƬ�ֱ���Խ�ߣ�ת��ʱ��Ҳ��Խ��
		ImageIO.write(image, "JPG", new File(outFilePath));
		System.out.println("SVG to JPG Done");
		pdfDoc.close();
	}
}
