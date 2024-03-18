package lab1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Demo extends Component implements ActionListener {

    // ************************************
    // List of the options(Original, Negative); correspond to the cases:
    // ************************************

    String baseDescs[] = {
            "Original",
            "Add",
            "Subtract",
            "Multiply",
            "Divide",
            "AND",
            "OR",
            "XOR",
    };

    String operationsDescs[] = {
            "Negative",
            "PixelValueReScaling",
            "PixelValueShift",
            "NOT",
            "Logarithmic",
            "Power-Law",
            "Random LUT",
            "Bit Plane Slicing",
            "equalizeHistogram",
            "Convolution"
    };

    int opIndex; // option index for
    int lastOp;

    private BufferedImage bi, bi2, biFiltered; // the input image saved as bi;//
    private ArrayList<BufferedImage> history;
    int w, h;

    public Demo() {
        LoadNewImage("PeppersRGB.bmp");
        bi2 = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        history = new ArrayList<>();
        biFiltered = bi;
        history.add(biFiltered);
    }

    private void LoadNewImage(String path) {
        try {
            bi = ImageIO.read(new File(path));

            w = bi.getWidth(null);
            h = bi.getHeight(null);
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage tbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = tbi.getGraphics();
                big.drawImage(bi, 0, 0, null);
                bi = tbi;
            }
        } catch (Exception e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
        repaint();
    }

    private void LoadNewImage2(String path) {
        try {
            bi2 = ImageIO.read(new File(path));

            w = bi2.getWidth(null);
            h = bi2.getHeight(null);
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage tbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = tbi.getGraphics();
                big.drawImage(bi2, 0, 0, null);
                bi2 = tbi;
            }
        } catch (Exception e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
        repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(w * 3, h);
    }

    String[] getDescriptions() {
        return operationsDescs;
    }

    String[] getDescriptionsBase() {
        return baseDescs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = { "bmp", "gif", "jpeg", "jpg", "png" };
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }

    void setOpIndexBase(int i) {
        opIndex = i;
        filterImageBase();

        history.add(biFiltered);
    }

    void setOpIndex(int i) {
        opIndex = i;
        filterImage();

        history.add(biFiltered);
    }

    void undo() {
        if (history.size() > 1) {
            history.remove(history.size() - 1);
            biFiltered = history.get(history.size() - 1);
        }
    }

    public void paint(Graphics g) { // Repaint will call this function so the image will change.
        g.drawImage(bi, 0, 0, null);
        g.drawImage(bi2, w, 0, null);
        g.drawImage(biFiltered, w * 2, 0, null);
    }

    // ************************************
    // Convert the Buffered Image to Array
    // ************************************
    private static int[][][] convertToArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[][][] result = new int[width][height][4];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = image.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                result[x][y][0] = a;
                result[x][y][1] = r;
                result[x][y][2] = g;
                result[x][y][3] = b;
            }
        }
        return result;
    }

    // ************************************
    // Convert the Array to BufferedImage
    // ************************************
    public BufferedImage convertToBimage(int[][][] TmpArray) {

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];

                // set RGB value

                int p = (a << 24) | (r << 16) | (g << 8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }
        return tmpimg;
    }

    // ************************************
    // Example: Image Negative
    // ************************************
    public BufferedImage ImageNegative(BufferedImage timg) {
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg); // Convert the image to array

        // Image Negative Operation:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                ImageArray[x][y][1] = 255 - ImageArray[x][y][1]; // r
                ImageArray[x][y][2] = 255 - ImageArray[x][y][2]; // g
                ImageArray[x][y][3] = 255 - ImageArray[x][y][3]; // b
            }
        }

        return convertToBimage(ImageArray); // Convert the array to BufferedImage
    }

    public static BufferedImage PixelValueReScaling(BufferedImage timg, double scale) {
        int width = timg.getWidth();
        int height = timg.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = timg.getRGB(x, y);
                int red = (int) (((rgb >> 16) & 0xff) * scale);
                int green = (int) (((rgb >> 8) & 0xff) * scale);
                int blue = (int) ((rgb & 0xff) * scale);

                // Clamp values to the range [0, 255]
                red = Math.min(Math.max(red, 0), 255);
                green = Math.min(Math.max(green, 0), 255);
                blue = Math.min(Math.max(blue, 0), 255);

                // Combine the color components and set the pixel
                int newRGB = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRGB);
            }
        }

        return result;
    }

    public static BufferedImage PixelValueShift(BufferedImage timg, int shift) {
        int width = timg.getWidth();
        int height = timg.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Iterate over each pixel and perform shifting
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get RGB value of the pixel
                int rgb = timg.getRGB(x, y);

                // Extract red, green, and blue components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Apply shifting to each component
                red = clamp(red + shift);
                green = clamp(green + shift);
                blue = clamp(blue + shift);

                // Combine the components into a new RGB value
                int newRGB = (red << 16) | (green << 8) | blue;

                // Set the new pixel value in the image
                result.setRGB(x, y, newRGB);
            }
        }
        return result;
    }

    public static BufferedImage addImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int red = Math.min(((rgb1 >> 16) & 0xff) + ((rgb2 >> 16) & 0xff), 255);
                int green = Math.min(((rgb1 >> 8) & 0xff) + ((rgb2 >> 8) & 0xff), 255);
                int blue = Math.min((rgb1 & 0xff) + (rgb2 & 0xff), 255);

                int newRgb = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage subtractImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int red = Math.max(((rgb1 >> 16) & 0xff) - ((rgb2 >> 16) & 0xff), 0);
                int green = Math.max(((rgb1 >> 8) & 0xff) - ((rgb2 >> 8) & 0xff), 0);
                int blue = Math.max((rgb1 & 0xff) - (rgb2 & 0xff), 0);

                int newRgb = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage multiplyImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int red = Math.min(((rgb1 >> 16) & 0xff) * ((rgb2 >> 16) & 0xff) / 255, 255);
                int green = Math.min(((rgb1 >> 8) & 0xff) * ((rgb2 >> 8) & 0xff) / 255, 255);
                int blue = Math.min((rgb1 & 0xff) * (rgb2 & 0xff) / 255, 255);

                int newRgb = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage divideImages(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int red = divideColorComponent((rgb1 >> 16) & 0xff, (rgb2 >> 16) & 0xff);
                int green = divideColorComponent((rgb1 >> 8) & 0xff, (rgb2 >> 8) & 0xff);
                int blue = divideColorComponent(rgb1 & 0xff, rgb2 & 0xff);

                int newRgb = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    private static int divideColorComponent(int color1, int color2) {
        if (color2 == 0) {
            return 255;
        }
        return Math.min(color1 * 255 / color2, 255);
    }

    public static BufferedImage applyBitwiseNot(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);

                // Apply NOT operation to each of the RGB components
                int red = (~(rgb >> 16) & 0xff);
                int green = (~(rgb >> 8) & 0xff);
                int blue = (~rgb & 0xff);

                // Reassemble the pixel with its new color values
                int newRgb = (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb | 0xFF000000); // Ensure the alpha channel is set to opaque
            }
        }
        return result;
    }

    public static BufferedImage applyBitwiseOperation(BufferedImage img1, BufferedImage img2, String operation) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                int newRgb;
                switch (operation) {
                    case "AND":
                        newRgb = rgb1 & rgb2;
                        break;
                    case "OR":
                        newRgb = rgb1 | rgb2;
                        break;
                    case "XOR":
                        newRgb = rgb1 ^ rgb2;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid operation: " + operation);
                }
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage applyLogarithmicTransformation(BufferedImage img, double c) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int red = (int) (c * Math.log(1 + ((rgb >> 16) & 0xff)));
                int green = (int) (c * Math.log(1 + ((rgb >> 8) & 0xff)));
                int blue = (int) (c * Math.log(1 + (rgb & 0xff)));

                // Clamp the values to be within 0-255
                red = Math.min(Math.max(red, 0), 255);
                green = Math.min(Math.max(green, 0), 255);
                blue = Math.min(Math.max(blue, 0), 255);

                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage applyPowerLawTransformation(BufferedImage img, double c, double gamma) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int red = (int) (c * Math.pow((rgb >> 16) & 0xff, gamma));
                int green = (int) (c * Math.pow((rgb >> 8) & 0xff, gamma));
                int blue = (int) (c * Math.pow(rgb & 0xff, gamma));

                // Clamp the values to be within 0-255
                red = Math.min(Math.max(red, 0), 255);
                green = Math.min(Math.max(green, 0), 255);
                blue = Math.min(Math.max(blue, 0), 255);

                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    private static int[] generateRandomLUT() {
        Random random = new Random();
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = random.nextInt(256);
        }
        return lut;
    }

    private static BufferedImage applyLUT(BufferedImage img, int[] lut) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xff;
                int red = lut[(rgb >> 16) & 0xff];
                int green = lut[(rgb >> 8) & 0xff];
                int blue = lut[rgb & 0xff];

                int newRgb = (alpha << 24) | (red << 16) | (green << 8) | blue;
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static BufferedImage getBitPlane(BufferedImage img, int bitPosition) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        int mask = 1 << bitPosition;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int gray = (int) (((rgb & 0x00ff0000) >> 16) * 0.299
                        + ((rgb & 0x0000ff00) >> 8) * 0.587
                        + (rgb & 0x000000ff) * 0.114);

                int bit = (gray & mask) != 0 ? 255 : 0;

                int newRgb = (0xFF << 24) | (bit << 16) | (bit << 8) | bit; // Setting the extracted bit for RGB
                result.setRGB(x, y, newRgb);
            }
        }
        return result;
    }

    public static int[][] findColorHistogram(BufferedImage img) {
        int[][] histograms = new int[3][256]; // 0: Red, 1: Green, 2: Blue

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color color = new Color(img.getRGB(x, y));
                histograms[0][color.getRed()]++;
                histograms[1][color.getGreen()]++;
                histograms[2][color.getBlue()]++;
            }
        }

        return histograms;
    }

    public static BufferedImage equalizeColorHistogram(BufferedImage img) {
        int[][] histograms = findColorHistogram(img);
        int width = img.getWidth();
        int height = img.getHeight();
        int totalPixels = width * height;
        int[][] equalizedHistograms = new int[3][256];

        for (int i = 0; i < 3; i++) {
            float[] cdf = new float[256];
            cdf[0] = histograms[i][0];
            for (int j = 1; j < 256; j++) {
                cdf[j] = cdf[j - 1] + histograms[i][j];
            }
            // Create the mapping for each a single channel
            for (int j = 0; j < 256; j++) {
                equalizedHistograms[i][j] = Math.round(cdf[j] * 255 / totalPixels);
            }
        }

        // apply LUT to all channels
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(img.getRGB(x, y));
                int red = equalizedHistograms[0][color.getRed()];
                int green = equalizedHistograms[1][color.getGreen()];
                int blue = equalizedHistograms[2][color.getBlue()];
                Color newColor = new Color(red, green, blue);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    public static BufferedImage convoluteImage(BufferedImage img, float[][] mask, boolean absCorrection) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int maskHeight = mask.length;
        int maskWidth = mask[0].length;
        int maskOffset = maskWidth / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float redSum = 0, greenSum = 0, blueSum = 0;
                float max = 0;
                for (int i = 0; i < maskHeight; i++) {
                    for (int j = 0; j < maskWidth; j++) {
                        int pixelX = x + j - maskOffset;
                        int pixelY = y + i - maskOffset;

                        if (pixelX < 0 || pixelX >= width || pixelY < 0 || pixelY >= height) {
                            continue; // Skip pixels outside of the image boundaries
                        }

                        Color pixelColor = new Color(img.getRGB(pixelX, pixelY));
                        float maskValue = mask[i][j];

                        redSum += pixelColor.getRed() * maskValue;
                        greenSum += pixelColor.getGreen() * maskValue;
                        blueSum += pixelColor.getBlue() * maskValue;
                        max = Math.max(max, Math.max(Math.abs(redSum), Math.max(Math.abs(greenSum), Math.abs(blueSum))));
                    }
                }



                if(absCorrection)
                {
                    redSum = (int)Math.abs((redSum / max)*255);
                    greenSum = (int)Math.abs((greenSum / max)*255);
                    blueSum = (int)Math.abs((blueSum / max)*255);
                }


                Color newColor = new Color(clamp((int)redSum), clamp((int)greenSum), clamp((int)blueSum));
                result.setRGB(x, y, newColor.getRGB());
            }
        }

        return result;
    }

    // ************************************
    // Your turn now: Add more function below
    // ************************************

    // Clamp the value between 0 and 255
    private static int clamp(int value) {
        return Math.max(0, Math.min(value, 255));
    }

    // ************************************
    // You need to register your functioin here
    // ************************************

    public void filterImageBase() {
        lastOp = opIndex;
        switch (opIndex) {
            case 0:
                biFiltered = bi; /* original */
                return;

            case 1:
                biFiltered = addImages(bi, bi2);
                return;

            case 2:
                biFiltered = subtractImages(bi, bi2);
                return;

            case 3:
                biFiltered = multiplyImages(bi, bi2);
                return;

            case 4:
                biFiltered = divideImages(bi, bi2);
                return;

            case 5:
                biFiltered = applyBitwiseOperation(bi, bi2, "AND");
                return;

            case 6:
                biFiltered = applyBitwiseOperation(bi, bi2, "OR");
                return;

            case 7:
                biFiltered = applyBitwiseOperation(bi, bi2, "XOR");
                return;
        }

    }

    public void filterImage() {
        /*
         * if (opIndex == lastOp) {
         * return;
         * }
         */

        lastOp = opIndex;
        switch (opIndex) {
            case 0:
                biFiltered = ImageNegative(biFiltered); /* Image Negative */
                return;

            case 1:
                while (true) {
                    String scaleString = JOptionPane.showInputDialog("Enter Scale:");
                    if (scaleString == null) {
                        return;
                    }
                    if (scaleString.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                        biFiltered = PixelValueReScaling(biFiltered, Double.parseDouble(scaleString));
                        return;
                    }
                }

            case 2:
                while (true) {
                    String shiftString = JOptionPane.showInputDialog("Enter Shift Ammount:");
                    if (shiftString == null) {
                        return;
                    }
                    if (shiftString.matches("^-?\\d+$")) {
                        biFiltered = PixelValueShift(biFiltered, Integer.parseInt(shiftString));
                        return;
                    }
                }

            case 3:
                biFiltered = applyBitwiseNot(biFiltered);
                return;

            case 4:
                while (true) {
                    String c = JOptionPane.showInputDialog("Enter c:");
                    if (c == null) {
                        return;
                    }
                    if (c.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                        biFiltered = applyLogarithmicTransformation(biFiltered, Double.parseDouble(c));
                        return;
                    }
                }

            case 5:
                while (true) {
                    String c = JOptionPane.showInputDialog("Enter c:");
                    String p = JOptionPane.showInputDialog("Enter p:");
                    if (c == null || p == null) {
                        return;
                    }
                    if (c.matches("[0-9]{1,13}(\\.[0-9]*)?") && p.matches("[0-9]{1,13}(\\.[0-9]*)?")) {
                        biFiltered = applyPowerLawTransformation(biFiltered, Double.parseDouble(c),
                                Double.parseDouble(p));
                        return;
                    }
                }

            case 6:
                biFiltered = applyLUT(biFiltered, generateRandomLUT());
                return;

            case 7:
                while (true) {
                    String p = JOptionPane.showInputDialog("Enter bit plane:");
                    if (p == null) {
                        return;
                    }
                    if (p.matches("[0-9]+")) {
                        biFiltered = getBitPlane(biFiltered, Math.min(7, Math.max(Integer.parseInt(p), 0)));
                        return;
                    }
                }

            case 8:
                biFiltered = equalizeColorHistogram(biFiltered);
                return;

            case 9:
                String[] options = {
                        "Averaging",
                        "Weighted averaging",
                        "4 neighbour Laplacian",
                        "8 neighbour Laplacian",
                        "4 neighbour Laplacian Enhancement",
                        "8 neighbour Laplacian Enhancement",
                        "Roberts 1",
                        "Roberts 2",
                        "Sobel X",
                        "Sobel Y"
                    };
                // ...and passing `frame` instead of `null` as first parameter
                Object selectionObject = JOptionPane.showInputDialog(this, "Choose", "Menu", JOptionPane.PLAIN_MESSAGE,
                        null, options, options[0]);
                String selection = selectionObject.toString();

                float[][] mask = new float[][] {
                        { 0f, 0f, 0f },
                        { 0f, 1f, 0f },
                        { 0f, 0f, 0f }
                };
                if (selection.equals(options[0])) {
                    mask = new float[][] {
                            { 1 / 9f, 1 / 9f, 1 / 9f },
                            { 1 / 9f, 1 / 9f, 1 / 9f },
                            { 1 / 9f, 1 / 9f, 1 / 9f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                if (selection.equals(options[1])) {
                    mask = new float[][] {
                            { 1 / 16f, 2 / 16f, 1 / 16f },
                            { 2 / 16f, 4 / 16f, 2 / 16f },
                            { 1 / 16f, 2 / 16f, 1 / 16f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                if (selection.equals(options[2])) {
                    mask = new float[][] {
                            { 0f, -1f, 0f },
                            { -1f, 4f, -1f },
                            { 0f, -1f, 0f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                if (selection.equals(options[3])) {
                    mask = new float[][] {
                            { -1f, -1f, -1f },
                            { -1f, 8f, -1f },
                            { -1f, -1f, -1f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                
                if (selection.equals(options[4])) {
                    mask = new float[][] {
                            { 0f, -1f, 0f },
                            { -1f, 5f, -1f },
                            { 0f, -1f, 0f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                if (selection.equals(options[5])) {
                    mask = new float[][] {
                            { -1f, -1f, -1f },
                            { -1f, 9f, -1f },
                            { -1f, -1f, -1f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, false);
                    return;
                }

                if (selection.equals(options[6])) {
                    mask = new float[][] {
                            { 0f, 0f, 0f },
                            { 0f, 0f, -1f },
                            { 0f, 1f, 0f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, true);
                    return;
                }

                if (selection.equals(options[7])) {
                    mask = new float[][] {
                            { 0f, 0f, 0f },
                            { 0f, -1f, 0f },
                            { 0f, 0f, 1f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, true);
                    return;
                }

                if (selection.equals(options[8])) {
                    mask = new float[][] {
                            { -1f, 0f, 1f },
                            { -2f, 0f, 2f },
                            { -1f, 0f, 1f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, true);
                    return;
                }

                if (selection.equals(options[9])) {
                    mask = new float[][] {
                            { -1f, -2f, -1f },
                            { 0f, 0f, 0f },
                            { 1f, 2f, 1f }
                    };
                    biFiltered = convoluteImage(biFiltered, mask, true);
                    return;
                }
        }

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("SetFilter")) {
            JComboBox cb = (JComboBox) e.getSource();
            setOpIndex(cb.getSelectedIndex());
            repaint();
        } else if (e.getActionCommand().equals("SetFilterBase")) {
            JComboBox cb = (JComboBox) e.getSource();
            setOpIndexBase(cb.getSelectedIndex());
            repaint();
        } else if (e.getActionCommand().equals("Formats")) {
            JComboBox cb = (JComboBox) e.getSource();
            String format = (String) cb.getSelectedItem();
            File saveFile = new File("savedimage." + format);
            JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(cb);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {
                    ImageIO.write(biFiltered, format, saveFile);
                } catch (IOException ex) {
                }
            }
        } else if (e.getActionCommand().equals("OpenFile")) {
            // Create a file chooser
            final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.addChoosableFileFilter(
                    new FileNameExtensionFilter("Images (" + String.join(",", getFormats()) + ")", getFormats()));
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                this.LoadNewImage(file.getPath());
            }

        }

        else if (e.getActionCommand().equals("OpenFile2")) {
            // Create a file chooser
            final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
            fc.addChoosableFileFilter(
                    new FileNameExtensionFilter("Images (" + String.join(",", getFormats()) + ")", getFormats()));
            int returnVal = fc.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                this.LoadNewImage2(file.getPath());
            }
        }

        else if (e.getActionCommand().equals("undo")) {
            undo();
            repaint();
        }
    };

    public static void main(String s[]) {
        JFrame f = new JFrame("Image Processing Demo");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Demo de = new Demo();
        f.add("Center", de);

        JButton openButton = new JButton("Open 1");
        openButton.setActionCommand("OpenFile");
        openButton.addActionListener(de);

        JButton openButton2 = new JButton("Open 2");
        openButton2.setActionCommand("OpenFile2");
        openButton2.addActionListener(de);

        JComboBox baseChoices = new JComboBox(de.getDescriptionsBase());
        baseChoices.setActionCommand("SetFilterBase");
        baseChoices.addActionListener(de);

        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);

        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);

        JButton undoButton = new JButton("Undo");
        undoButton.setActionCommand("undo");
        undoButton.addActionListener(de);

        JPanel panel = new JPanel();
        panel.add(openButton);
        panel.add(openButton2);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        panel.add(baseChoices);
        panel.add(choices);
        panel.add(undoButton);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }
}
