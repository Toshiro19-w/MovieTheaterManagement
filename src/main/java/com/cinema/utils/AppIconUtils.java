package com.cinema.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.TranscoderException;

/**
 * Lớp tiện ích để quản lý biểu tượng ứng dụng với hỗ trợ SVG
 * Sử dụng Apache Batik để render SVG với chất lượng vector
 */
public class AppIconUtils {
    
    private static final Logger LOGGER = Logger.getLogger(AppIconUtils.class.getName());
    
    // Paths cho logo
    private static final String SVG_LOGO_PATH = "/images/Icon/LogoApp.svg";
    private static final String PNG_LOGO_PATH = "/images/Icon/LogoApp.png";
    private static final String FALLBACK_EMOJI = "🎬";
    private static final int DEFAULT_LOGO_SIZE = 40;
    
    // Cache cho các icon đã được render
    private static final Map<String, ImageIcon> iconCache = new ConcurrentHashMap<>();
    
    // Flag để biết loại logo nào available
    private static volatile Boolean hasSvgLogo;
    private static volatile ImageIcon fallbackPngLogo;
    
    /**
     * Đặt biểu tượng cho cửa sổ ứng dụng
     * @param frame Cửa sổ cần đặt biểu tượng
     * @throws IllegalArgumentException nếu frame là null
     */
    public static void setAppIcon(JFrame frame) {
        if (frame == null) {
            throw new IllegalArgumentException("Frame không được null");
        }
        
        try {
            // Ưu tiên SVG cho icon ứng dụng vì có thể scale tốt
            BufferedImage iconImage = renderSvgLogo(64, 64); // Kích thước chuẩn cho app icon
            
            if (iconImage != null) {
                frame.setIconImage(iconImage);
                LOGGER.info("Đã đặt biểu tượng SVG cho ứng dụng thành công");
            } else {
                // Fallback to PNG
                ImageIcon pngIcon = getPngFallback();
                if (pngIcon != null && pngIcon.getImage() != null) {
                    frame.setIconImage(pngIcon.getImage());
                    LOGGER.info("Đã đặt biểu tượng PNG cho ứng dụng thành công");
                } else {
                    LOGGER.warning("Không thể tải biểu tượng ứng dụng");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi đặt biểu tượng cho ứng dụng", e);
        }
    }
    
    /**
     * Tạo JLabel chứa logo ứng dụng với kích thước mặc định
     * @return JLabel chứa logo
     */
    public static JLabel getAppLogo() {
        return getAppLogo(DEFAULT_LOGO_SIZE, DEFAULT_LOGO_SIZE);
    }
    
    /**
     * Tạo JLabel chứa logo ứng dụng với kích thước tùy chỉnh
     * @param targetWidth Chiều rộng mục tiêu (phải > 0)
     * @param targetHeight Chiều cao mục tiêu (phải > 0)
     * @return JLabel chứa logo
     * @throws IllegalArgumentException nếu kích thước không hợp lệ
     */
    public static JLabel getAppLogo(int targetWidth, int targetHeight) {
        if (targetWidth <= 0 || targetHeight <= 0) {
            throw new IllegalArgumentException("Kích thước phải lớn hơn 0");
        }
        
        String cacheKey = targetWidth + "x" + targetHeight;
        ImageIcon cachedIcon = iconCache.get(cacheKey);
        
        if (cachedIcon == null) {
            // Ưu tiên SVG vì chất lượng vector
            BufferedImage svgImage = renderSvgLogo(targetWidth, targetHeight);
            
            if (svgImage != null) {
                cachedIcon = new ImageIcon(svgImage);
                iconCache.put(cacheKey, cachedIcon);
                return new JLabel(cachedIcon);
            }
            
            // Fallback to PNG scaling
            ImageIcon pngIcon = getPngFallback();
            if (pngIcon != null) {
                return createScaledPngLogo(pngIcon, targetWidth, targetHeight);
            }
            
            // Final fallback to emoji
            return createFallbackLogo();
        }
        
        return new JLabel(cachedIcon);
    }
    
    /**
     * Kiểm tra xem có SVG logo không
     */
    public static boolean hasSvgSupport() {
        return checkSvgAvailability();
    }
    
    /**
     * Xóa cache icon
     */
    public static void clearCache() {
        iconCache.clear();
        hasSvgLogo = null;
        fallbackPngLogo = null;
        LOGGER.info("Đã xóa cache icon");
    }
    
    /**
     * Render SVG logo với kích thước cụ thể
     * Thử nhiều phương pháp để đạt chất lượng tốt nhất
     */
    private static BufferedImage renderSvgLogo(int width, int height) {
        if (!checkSvgAvailability()) {
            return null;
        }
        
        try (InputStream svgInputStream = AppIconUtils.class.getResourceAsStream(SVG_LOGO_PATH)) {
            if (svgInputStream == null) {
                LOGGER.warning("Không tìm thấy file SVG: " + SVG_LOGO_PATH);
                return null;
            }
            
            // Thử render với Batik
            try {
                return renderSvgToImage(svgInputStream, width, height);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Batik render failed, trying alternative method", e);
                
                // Reset stream và thử phương pháp khác
                try (InputStream alternativeStream = AppIconUtils.class.getResourceAsStream(SVG_LOGO_PATH)) {
                    return renderSvgAlternative(alternativeStream, width, height);
                }
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Lỗi khi render SVG logo", e);
            return null;
        }
    }
    
    /**
     * Phương pháp alternative để render SVG (dùng khi Batik có vấn đề)
     * Sử dụng Java's built-in SVG support (nếu có)
     */
    private static BufferedImage renderSvgAlternative(InputStream svgInputStream, int width, int height) {
        try {
            // Đọc SVG content
            byte[] svgData = svgInputStream.readAllBytes();
            String svgContent = new String(svgData, "UTF-8");
            
            // Tạo một BufferedImage và render SVG bằng cách parse thủ công
            // (Đây là fallback method, có thể không hoàn hảo cho mọi SVG)
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            try {
                // Set high quality rendering
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                // Clear background
                g2d.setComposite(java.awt.AlphaComposite.Clear);
                g2d.fillRect(0, 0, width, height);
                g2d.setComposite(java.awt.AlphaComposite.SrcOver);
                
                // Simple fallback: tạo logo tương tự bằng Java2D
                renderSimpleLogo(g2d, width, height);
                
            } finally {
                g2d.dispose();
            }
            
            return image;
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Alternative SVG render failed", e);
            return null;
        }
    }
    
    /**
     * Render logo đơn giản bằng Java2D khi SVG render failed
     */
    private static void renderSimpleLogo(Graphics2D g2d, int width, int height) {
        // Scale factors
        float scale = Math.min(width / 400f, height / 400f);
        int offsetX = (width - (int)(400 * scale)) / 2;
        int offsetY = (height - (int)(400 * scale)) / 2;
        
        g2d.translate(offsetX, offsetY);
        g2d.scale(scale, scale);
        
        // Vẽ tam giác trái (solid)
        java.awt.GradientPaint leftGradient = new java.awt.GradientPaint(
            50, 100, new java.awt.Color(99, 102, 241),
            125, 300, new java.awt.Color(139, 92, 246)
        );
        g2d.setPaint(leftGradient);
        int[] leftX = {50, 200, 125};
        int[] leftY = {100, 100, 300};
        g2d.fillPolygon(leftX, leftY, 3);
        
        // Vẽ tam giác phải (outline)
        java.awt.GradientPaint rightGradient = new java.awt.GradientPaint(
            200, 100, new java.awt.Color(168, 85, 247),
            275, 300, new java.awt.Color(192, 132, 252)
        );
        g2d.setPaint(rightGradient);
        g2d.setStroke(new java.awt.BasicStroke(20, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        
        int[] rightX = {200, 350, 275};
        int[] rightY = {100, 100, 300};
        g2d.drawPolygon(rightX, rightY, 3);
        
        // Vẽ đường ngang
        g2d.drawLine(225, 260, 325, 260);
    }
    
    /**
     * Render SVG input stream thành BufferedImage với chất lượng cao
     */
    private static BufferedImage renderSvgToImage(InputStream svgInputStream, int width, int height) 
            throws TranscoderException {
        
        // Tăng độ phân giải render gấp đôi để tránh blur
        int renderWidth = width * 2;
        int renderHeight = height * 2;
        
        final BufferedImage[] resultImage = new BufferedImage[1];
        
        // Custom ImageTranscoder với cài đặt chất lượng cao
        ImageTranscoder transcoder = new ImageTranscoder() {
            @Override
            public BufferedImage createImage(int w, int h) {
                return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            
            @Override
            public void writeImage(BufferedImage img, TranscoderOutput output) {
                resultImage[0] = img;
            }
        };
        
        // Cài đặt các hint chất lượng cao
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) renderWidth);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) renderHeight);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
        
        // Cài đặt rendering quality
        transcoder.addTranscodingHint(ImageTranscoder.KEY_ALLOWED_SCRIPT_TYPES, "");
        transcoder.addTranscodingHint(ImageTranscoder.KEY_CONSTRAIN_SCRIPT_ORIGIN, true);
        transcoder.addTranscodingHint(ImageTranscoder.KEY_EXECUTE_ONLOAD, false);
        
        // Render với độ phân giải cao
        TranscoderInput input = new TranscoderInput(svgInputStream);
        transcoder.transcode(input, null);
        
        BufferedImage highResImage = resultImage[0];
        if (highResImage == null) {
            return null;
        }
        
        // Scale down với chất lượng cao để có kết quả sắc nét
        return scaleImageHighQuality(highResImage, width, height);
    }
    
    /**
     * Scale image với chất lượng cao nhất
     */
    private static BufferedImage scaleImageHighQuality(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage scaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledImage.createGraphics();
        
        try {
            // Cài đặt rendering hints tối ưu
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            
            // Xóa background để giữ transparency
            g2d.setComposite(java.awt.AlphaComposite.Clear);
            g2d.fillRect(0, 0, targetWidth, targetHeight);
            g2d.setComposite(java.awt.AlphaComposite.SrcOver);
            
            // Scale image
            g2d.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
            
        } finally {
            g2d.dispose();
        }
        
        return scaledImage;
    }
    
    /**
     * Kiểm tra SVG có available không
     */
    private static boolean checkSvgAvailability() {
        if (hasSvgLogo == null) {
            synchronized (AppIconUtils.class) {
                if (hasSvgLogo == null) {
                    try {
                        // Kiểm tra Apache Batik có trong classpath không
                        Class.forName("org.apache.batik.transcoder.image.ImageTranscoder");
                        
                        // Kiểm tra SVG file có tồn tại không
                        InputStream svgStream = AppIconUtils.class.getResourceAsStream(SVG_LOGO_PATH);
                        hasSvgLogo = (svgStream != null);
                        if (svgStream != null) {
                            svgStream.close();
                        }
                        
                        LOGGER.info("SVG support: " + hasSvgLogo);
                    } catch (Exception e) {
                        hasSvgLogo = false;
                        LOGGER.info("SVG support không available: " + e.getMessage());
                    }
                }
            }
        }
        return hasSvgLogo;
    }
    
    /**
     * Lấy PNG fallback logo
     */
    private static ImageIcon getPngFallback() {
        if (fallbackPngLogo == null) {
            synchronized (AppIconUtils.class) {
                if (fallbackPngLogo == null) {
                    try {
                        fallbackPngLogo = new ImageIcon(AppIconUtils.class.getResource(PNG_LOGO_PATH));
                        if (fallbackPngLogo.getImage() == null) {
                            fallbackPngLogo = null;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Không thể tải PNG fallback", e);
                        fallbackPngLogo = null;
                    }
                }
            }
        }
        return fallbackPngLogo;
    }
    
    /**
     * Tạo scaled PNG logo khi không có SVG
     */
    private static JLabel createScaledPngLogo(ImageIcon originalIcon, int targetWidth, int targetHeight) {
        // Sử dụng logic scaling cũ cho PNG
        Dimension scaledDimension = calculateScaledDimension(
            originalIcon.getIconWidth(), 
            originalIcon.getIconHeight(), 
            targetWidth, 
            targetHeight
        );
        
        BufferedImage scaledImage = new BufferedImage(
            scaledDimension.width, 
            scaledDimension.height, 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = scaledImage.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(originalIcon.getImage(), 0, 0, 
                         scaledDimension.width, scaledDimension.height, null);
        } finally {
            g2d.dispose();
        }
        
        return new JLabel(new ImageIcon(scaledImage));
    }
    
    /**
     * Tạo fallback logo khi không tải được icon
     */
    private static JLabel createFallbackLogo() {
        JLabel logoLabel = new JLabel(FALLBACK_EMOJI);
        logoLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 28));
        logoLabel.setForeground(new java.awt.Color(79, 70, 229));
        logoLabel.setToolTipText("Logo ứng dụng (fallback)");
        return logoLabel;
    }
    
    /**
     * Tính toán kích thước scaled với tỷ lệ khung hình được giữ nguyên
     */
    private static Dimension calculateScaledDimension(int originalWidth, int originalHeight, 
                                                    int targetWidth, int targetHeight) {
        double aspectRatio = (double) originalWidth / originalHeight;
        
        if (originalWidth > originalHeight) {
            targetHeight = (int) (targetWidth / aspectRatio);
        } else {
            targetWidth = (int) (targetHeight * aspectRatio);
        }
        
        return new Dimension(targetWidth, targetHeight);
    }
    
    /**
     * Inner class cho Dimension
     */
    private static class Dimension {
        final int width;
        final int height;
        
        Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}