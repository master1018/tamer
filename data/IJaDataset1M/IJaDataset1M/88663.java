package com.windsor.node.worker.schedule.processor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import com.windsor.node.common.domain.ActivityEntry;
import com.windsor.node.common.domain.CommonContentType;
import com.windsor.node.common.domain.CommonTransactionStatusCode;
import com.windsor.node.common.domain.Document;
import com.windsor.node.data.dao.TransactionDao;

public class FileSystemDataProcessor implements InitializingBean {

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TransactionDao transactionDao;

    public void afterPropertiesSet() {
        if (transactionDao == null) {
            throw new RuntimeException("TransactionDao Not Set");
        }
    }

    public List getAndSaveData(String transactionId, String filePath) {
        if (StringUtils.isBlank(transactionId)) {
            throw new RuntimeException("Null transactionId");
        }
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("Null filePath");
        }
        try {
            List info = new ArrayList();
            File fsFile = new File(filePath);
            if (!fsFile.exists()) {
                throw new IOException("File does not exist: " + filePath);
            }
            info.add(new ActivityEntry("Getting document..."));
            Document doc = new Document();
            doc.setContent(FileUtils.readFileToByteArray(fsFile));
            doc.setDocumentName(fsFile.getName());
            doc.setDocumentStatus(CommonTransactionStatusCode.Processing);
            doc.setDocumentStatusDetail("Results of schedule file retrieval from " + filePath + " performed on");
            String fsFileExt = FilenameUtils.getExtension(fsFile.getAbsolutePath());
            if (fsFileExt == null) {
                doc.setType(CommonContentType.BIN);
            } else if (fsFileExt.equalsIgnoreCase("xml")) {
                doc.setType(CommonContentType.XML);
            } else if (fsFileExt.equalsIgnoreCase("txt")) {
                doc.setType(CommonContentType.FLAT);
            } else if (fsFileExt.equalsIgnoreCase("zip")) {
                doc.setType(CommonContentType.ZIP);
            } else if (fsFileExt.equalsIgnoreCase("odf")) {
                doc.setType(CommonContentType.ODF);
            } else {
                doc.setType(CommonContentType.OTHER);
            }
            info.add(new ActivityEntry("Saving document..."));
            transactionDao.addDocument(transactionId, doc);
            info.add(new ActivityEntry("Document saved."));
            return info;
        } catch (Exception ex) {
            logger.error("Get And Save Data: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while retrieving source data: " + ex.getMessage(), ex);
        }
    }

    public List getAndSendData(String transactionId, String targetDirPath) {
        if (StringUtils.isBlank(transactionId)) {
            throw new RuntimeException("Null transactionId");
        }
        if (StringUtils.isBlank(targetDirPath)) {
            throw new RuntimeException("Null targetFilePath");
        }
        try {
            List info = new ArrayList();
            info.add(new ActivityEntry("Validating dir: " + targetDirPath));
            logger.debug("Checking dir: " + targetDirPath);
            File targetDir = new File(targetDirPath);
            if (!targetDir.exists()) {
                logger.debug("Creating dir: " + targetDirPath);
                info.add(new ActivityEntry("Creating dir: " + targetDirPath));
                FileUtils.forceMkdir(targetDir);
            }
            logger.debug("Getting documents for transaction: " + transactionId);
            info.add(new ActivityEntry("Getting documents..."));
            List docs = transactionDao.getDocuments(transactionId, false, true);
            if (docs == null || docs.size() < 1) {
                throw new RuntimeException("No documents to send. At least one required.");
            }
            logger.debug("Documents: " + docs.size());
            info.add(new ActivityEntry("Found: " + docs.size()));
            for (int i = 0; i < docs.size(); i++) {
                Document doc = (Document) docs.get(i);
                logger.debug("Doc: " + doc);
                info.add(new ActivityEntry("Document: " + doc.getDocumentName()));
                String targetFilePath = FilenameUtils.concat(targetDir.toString(), doc.getDocumentName());
                info.add(new ActivityEntry("Target: " + targetFilePath));
                logger.debug("Target File Path: " + doc);
                File targetFile = new File(targetFilePath);
                FileUtils.writeByteArrayToFile(targetFile, doc.getContent());
                logger.debug("Document written");
                info.add(new ActivityEntry("Document saved."));
            }
            return info;
        } catch (Exception ex) {
            logger.error("Get And Send Data: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while sending data: " + ex.getMessage(), ex);
        }
    }

    public void setTransactionDao(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }
}
