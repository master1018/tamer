package org.adempiere.webui.panel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ListItem;
import org.adempiere.webui.component.Listbox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.Window;
import org.adempiere.webui.window.FDialog;
import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.util.CLogger;
import org.adempiere.webui.EnvWeb;
import org.compiere.util.Msg;
import org.compiere.util.Trx;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.au.AuScript;
import org.zkoss.zk.au.out.AuEcho;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkex.zul.Borderlayout;
import org.zkoss.zkex.zul.Center;
import org.zkoss.zkex.zul.North;
import org.zkoss.zkex.zul.South;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Timer;

/**
 * 
 * @author Low Heng Sin
 *
 */
@SuppressWarnings("deprecation")
public class WAttachment extends Window implements EventListener {

    private static final long serialVersionUID = 1L;

    private static CLogger log = CLogger.getCLogger(WAttachment.class);

    /**	Window No				*/
    private int m_WindowNo;

    /** Attachment				*/
    private MAttachment m_attachment;

    /** Change					*/
    private boolean m_change = false;

    private Iframe preview = new Iframe();

    private Textbox text = new Textbox();

    private Listbox cbContent = new Listbox();

    private Button bDelete = new Button();

    private Button bSave = new Button();

    private Button bDeleteAll = new Button();

    private Button bLoad = new Button();

    private Button bCancel = new Button();

    private Button bOk = new Button();

    private Button bRefresh = new Button();

    private Panel previewPanel = new Panel();

    private Borderlayout mainPanel = new Borderlayout();

    private Hbox toolBar = new Hbox();

    private Hbox confirmPanel = new Hbox();

    private int displayIndex;

    /**
	 *	Constructor.
	 *	loads Attachment, if ID <> 0
	 *  @param WindowNo window no
	 *  @param AD_Attachment_ID attachment
	 *  @param AD_Table_ID table
	 *  @param Record_ID record key
	 *  @param trxName transaction
	 */
    public WAttachment(int WindowNo, int AD_Attachment_ID, int AD_Table_ID, int Record_ID, Trx trx) {
        super();
        log.config("ID=" + AD_Attachment_ID + ", Table=" + AD_Table_ID + ", Record=" + Record_ID);
        m_WindowNo = WindowNo;
        try {
            staticInit();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "", ex);
        }
        String trxName = trx.getTrxName();
        if (AD_Attachment_ID == 0) m_attachment = new MAttachment(EnvWeb.getCtx(), AD_Table_ID, Record_ID, trxName); else m_attachment = new MAttachment(EnvWeb.getCtx(), AD_Attachment_ID, trxName);
        loadAttachments();
        try {
            setAttribute(Window.MODE_KEY, Window.MODE_HIGHLIGHTED);
            AEnv.showWindow(this);
            displayData(0, true);
            String script = "setTimeout(\"$e('" + preview.getUuid() + "').src = $e('" + preview.getUuid() + "').src\", 1000)";
            Clients.response(new AuScript(null, script));
            doModal();
        } catch (Exception e) {
        }
    }

    /**
	 *	Static setup.
	 *  <pre>
	 *  - northPanel
	 *      - toolBar
	 *      - title
	 *  - centerPane [split]
	 * 		- previewPanel (left)
	 *  	- text (right)
	 *  - confirmPanel
	 *  </pre>
	 *  @throws Exception
	 */
    void staticInit() throws Exception {
        this.setMaximizable(true);
        this.setWidth("700px");
        this.setHeight("600px");
        this.setTitle("Attachment");
        this.setClosable(true);
        this.setSizable(true);
        this.setBorder("normal");
        this.appendChild(mainPanel);
        mainPanel.setHeight("100%");
        mainPanel.setWidth("100%");
        North northPanel = new North();
        northPanel.setCollapsible(false);
        northPanel.setSplittable(false);
        cbContent.setMold("select");
        cbContent.setRows(0);
        cbContent.addEventListener(Events.ON_SELECT, this);
        toolBar.appendChild(bLoad);
        toolBar.appendChild(bDelete);
        toolBar.appendChild(bSave);
        toolBar.appendChild(cbContent);
        mainPanel.appendChild(northPanel);
        Div div = new Div();
        div.appendChild(toolBar);
        text.setRows(3);
        text.setWidth("100%");
        div.appendChild(text);
        northPanel.appendChild(div);
        bSave.setEnabled(false);
        bSave.setImage("/images/Export24.png");
        bSave.setTooltiptext(Msg.getMsg(EnvWeb.getCtx(), "AttachmentSave"));
        bSave.addEventListener(Events.ON_CLICK, this);
        bLoad.setImage("/images/Import24.png");
        bLoad.setTooltiptext(Msg.getMsg(EnvWeb.getCtx(), "Load"));
        bLoad.addEventListener(Events.ON_CLICK, this);
        bDelete.setImage("/images/Delete24.png");
        bDelete.setTooltiptext(Msg.getMsg(EnvWeb.getCtx(), "Delete"));
        bDelete.addEventListener(Events.ON_CLICK, this);
        previewPanel.appendChild(preview);
        preview.setHeight("100%");
        preview.setWidth("100%");
        Center centerPane = new Center();
        centerPane.setAutoscroll(true);
        centerPane.setFlex(true);
        mainPanel.appendChild(centerPane);
        centerPane.appendChild(previewPanel);
        South southPane = new South();
        mainPanel.appendChild(southPane);
        southPane.appendChild(confirmPanel);
        southPane.setHeight("30px");
        bCancel.setImage("/images/Cancel24.png");
        bCancel.addEventListener(Events.ON_CLICK, this);
        bOk.setImage("/images/Ok24.png");
        bOk.addEventListener(Events.ON_CLICK, this);
        bDeleteAll.setImage("/images/Delete24.png");
        bDeleteAll.addEventListener(Events.ON_CLICK, this);
        bRefresh.setImage("/images/Refresh24.png");
        bRefresh.addEventListener(Events.ON_CLICK, this);
        confirmPanel.appendChild(bDeleteAll);
        confirmPanel.appendChild(bRefresh);
        confirmPanel.appendChild(bCancel);
        confirmPanel.appendChild(bOk);
    }

    /**
	 * 	Dispose
	 */
    public void dispose() {
        preview = null;
        this.detach();
    }

    /**
	 *	Load Attachments
	 */
    private void loadAttachments() {
        log.config("");
        String sText = m_attachment.getTextMsg();
        if (sText == null) text.setText(""); else text.setText(sText);
        int size = m_attachment.getEntryCount();
        for (int i = 0; i < size; i++) cbContent.appendItem(m_attachment.getEntryName(i), m_attachment.getEntryName(i));
        if (size > 0) {
            cbContent.setSelectedIndex(0);
        }
    }

    /**
	 *  Display gif or jpg in gifPanel
	 * 	@param index index
	 */
    private void displayData(int index, boolean immediate) {
        preview.setSrc(null);
        bDelete.setEnabled(false);
        bSave.setEnabled(false);
        displayIndex = index;
        if (immediate) displaySelected(); else Clients.response(new AuEcho(this, "displaySelected", null));
    }

    /**
	 * Use to refresh preview frame, don't call directly.
	 */
    public void displaySelected() {
        MAttachmentEntry entry = m_attachment.getEntry(displayIndex);
        log.config("Index=" + displayIndex + " - " + entry);
        if (entry != null && entry.getData() != null) {
            bSave.setEnabled(true);
            bDelete.setEnabled(true);
            log.config(entry.toStringX());
            try {
                AMedia media = new AMedia(entry.getName(), null, entry.getContentType(), entry.getData());
                preview.setContent(media);
                preview.setVisible(true);
                preview.invalidate();
            } catch (Exception e) {
                log.log(Level.SEVERE, "attachment", e);
            }
        }
    }

    /**
	 * 	Get File Name with index
	 *	@param index index
	 *	@return file name or null
	 */
    private String getFileName(int index) {
        String fileName = null;
        if (cbContent.getItemCount() > index) {
            ListItem listitem = cbContent.getItemAtIndex(index);
            fileName = (String) listitem.getValue();
        }
        return fileName;
    }

    /**
	 *	Action Listener
	 *  @param e event
	 */
    public void onEvent(Event e) {
        if (e.getTarget() == bOk) {
            String newText = text.getText();
            if (newText == null) newText = "";
            String oldText = m_attachment.getTextMsg();
            if (oldText == null) oldText = "";
            if (!m_change) m_change = !newText.equals(oldText);
            if (newText.length() > 0 || m_attachment.getEntryCount() > 0) {
                if (m_change) {
                    m_attachment.setTextMsg(text.getText());
                    m_attachment.save();
                }
            } else m_attachment.delete(true);
            dispose();
        } else if (e.getTarget() == bCancel) {
            dispose();
        } else if (e.getTarget() == bDeleteAll) {
            deleteAttachment();
            dispose();
        } else if (e.getTarget() == bDelete) deleteAttachmentEntry(); else if (e.getTarget() == cbContent) displayData(cbContent.getSelectedIndex(), false); else if (e.getTarget() == bLoad) loadFile(); else if (e.getTarget() == bSave) saveAttachmentToFile(); else if (e.getTarget() == bRefresh) displayData(displayIndex, true); else if (e.getTarget() instanceof Timer) displayData(displayIndex, true);
    }

    /**************************************************************************
	 *	Load file for attachment
	 */
    private void loadFile() {
        log.info("");
        preview.setVisible(false);
        Media media = null;
        try {
            media = Fileupload.get(true);
            if (media != null) {
                ;
            } else {
                preview.setVisible(true);
                preview.invalidate();
                return;
            }
        } catch (InterruptedException e) {
            log.log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        String fileName = media.getName();
        log.config(fileName);
        int cnt = m_attachment.getEntryCount();
        for (int i = 0; i < cnt; i++) {
            if (m_attachment.getEntryName(i).equals(fileName)) {
                cbContent.setSelectedIndex(i);
                displayData(cbContent.getSelectedIndex(), false);
                m_change = true;
                return;
            }
        }
        if (m_attachment.addEntry(fileName, getMediaData(media))) {
            cbContent.appendItem(media.getName(), media.getName());
            cbContent.setSelectedIndex(cbContent.getItemCount() - 1);
            displayData(cbContent.getSelectedIndex(), false);
            m_change = true;
        }
    }

    private byte[] getMediaData(Media media) {
        byte[] bytes = null;
        if (media.inMemory()) bytes = media.getByteData(); else {
            InputStream is = media.getStreamData();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1000];
            int byteread = 0;
            try {
                while ((byteread = is.read(buf)) != -1) baos.write(buf, 0, byteread);
            } catch (IOException e) {
                log.log(Level.SEVERE, e.getLocalizedMessage(), e);
                throw new IllegalStateException(e.getLocalizedMessage());
            }
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    /**
	 *	Delete entire Attachment
	 */
    private void deleteAttachment() {
        log.info("");
        if (FDialog.ask(m_WindowNo, this, "AttachmentDelete?")) m_attachment.delete(true);
    }

    /**
	 *	Delete Attachment Entry
	 */
    private void deleteAttachmentEntry() {
        log.info("");
        int index = cbContent.getSelectedIndex();
        String fileName = getFileName(index);
        if (fileName == null) return;
        if (FDialog.ask(m_WindowNo, this, "AttachmentDeleteEntry?")) {
            if (m_attachment.deleteEntry(index)) cbContent.removeItemAt(index);
            m_change = true;
        }
    }

    /**
	 *	Save Attachment to File
	 */
    private void saveAttachmentToFile() {
        int index = cbContent.getSelectedIndex();
        log.info("index=" + index);
        if (m_attachment.getEntryCount() < index) return;
        MAttachmentEntry entry = m_attachment.getEntry(index);
        if (entry != null && entry.getData() != null) {
            try {
                AMedia media = new AMedia(entry.getName(), null, entry.getContentType(), entry.getData());
                Filedownload.save(media);
            } catch (Exception e) {
                log.log(Level.SEVERE, "attachment", e);
            }
        }
    }
}
