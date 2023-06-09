package com.google.zxing.datamatrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.DecoderResult;
import com.google.zxing.common.DetectorResult;
import com.google.zxing.datamatrix.decoder.Decoder;
import com.google.zxing.datamatrix.detector.Detector;
import java.util.Hashtable;

/**
 * This implementation can detect and decode Data Matrix codes in an image.
 *
 * @author bbrown@google.com (Brian Brown)
 */
public final class DataMatrixReader implements Reader {

    private static final ResultPoint[] NO_POINTS = new ResultPoint[0];

    private final Decoder decoder = new Decoder();

    /**
   * Locates and decodes a Data Matrix code in an image.
   *
   * @return a String representing the content encoded by the Data Matrix code
   * @throws NotFoundException if a Data Matrix code cannot be found
   * @throws FormatException if a Data Matrix code cannot be decoded
   * @throws ChecksumException if error correction fails
   */
    public Result decode(BinaryBitmap image) throws NotFoundException, ChecksumException, FormatException {
        return decode(image, null);
    }

    public Result decode(BinaryBitmap image, Hashtable hints) throws NotFoundException, ChecksumException, FormatException {
        DecoderResult decoderResult;
        ResultPoint[] points;
        if (hints != null && hints.containsKey(DecodeHintType.PURE_BARCODE)) {
            BitMatrix bits = extractPureBits(image.getBlackMatrix());
            decoderResult = decoder.decode(bits);
            points = NO_POINTS;
        } else {
            DetectorResult detectorResult = new Detector(image.getBlackMatrix()).detect();
            decoderResult = decoder.decode(detectorResult.getBits());
            points = detectorResult.getPoints();
        }
        Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.DATAMATRIX);
        if (decoderResult.getByteSegments() != null) {
            result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, decoderResult.getByteSegments());
        }
        if (decoderResult.getECLevel() != null) {
            result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, decoderResult.getECLevel().toString());
        }
        return result;
    }

    public void reset() {
    }

    /**
   * This method detects a Data Matrix code in a "pure" image -- that is, pure monochrome image
   * which contains only an unrotated, unskewed, image of a Data Matrix code, with some white border
   * around it. This is a specialized method that works exceptionally fast in this special
   * case.
   */
    private static BitMatrix extractPureBits(BitMatrix image) throws NotFoundException {
        int height = image.getHeight();
        int width = image.getWidth();
        int minDimension = Math.min(height, width);
        int borderWidth = 0;
        while (borderWidth < minDimension && !image.get(borderWidth, borderWidth)) {
            borderWidth++;
        }
        if (borderWidth == minDimension) {
            throw NotFoundException.getNotFoundInstance();
        }
        int moduleEnd = borderWidth + 1;
        while (moduleEnd < width && image.get(moduleEnd, borderWidth)) {
            moduleEnd++;
        }
        if (moduleEnd == width) {
            throw NotFoundException.getNotFoundInstance();
        }
        int moduleSize = moduleEnd - borderWidth;
        int columnEndOfSymbol = height - 1;
        while (columnEndOfSymbol >= 0 && !image.get(borderWidth, columnEndOfSymbol)) {
            columnEndOfSymbol--;
        }
        if (columnEndOfSymbol < 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        columnEndOfSymbol++;
        if ((columnEndOfSymbol - borderWidth) % moduleSize != 0) {
            throw NotFoundException.getNotFoundInstance();
        }
        int dimension = (columnEndOfSymbol - borderWidth) / moduleSize;
        borderWidth += moduleSize >> 1;
        int sampleDimension = borderWidth + (dimension - 1) * moduleSize;
        if (sampleDimension >= width || sampleDimension >= height) {
            throw NotFoundException.getNotFoundInstance();
        }
        BitMatrix bits = new BitMatrix(dimension);
        for (int i = 0; i < dimension; i++) {
            int iOffset = borderWidth + i * moduleSize;
            for (int j = 0; j < dimension; j++) {
                if (image.get(borderWidth + j * moduleSize, iOffset)) {
                    bits.set(j, i);
                }
            }
        }
        return bits;
    }
}
