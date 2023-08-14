public class SmilHelper {
    private static final String TAG = "Mms/smil";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    public static final String ELEMENT_TAG_TEXT = "text";
    public static final String ELEMENT_TAG_IMAGE = "img";
    public static final String ELEMENT_TAG_AUDIO = "audio";
    public static final String ELEMENT_TAG_VIDEO = "video";
    public static final String ELEMENT_TAG_REF = "ref";
    private SmilHelper() {
    }
    public static SMILDocument getDocument(PduBody pb) {
        PduPart smilPart = findSmilPart(pb);
        SMILDocument document = null;
        if (smilPart != null) {
            document = getSmilDocument(smilPart);
        }
        if (document == null) {
            document = createSmilDocument(pb);
        }
        return document;
    }
    public static SMILDocument getDocument(SlideshowModel model) {
        return createSmilDocument(model);
    }
    private static PduPart findSmilPart(PduBody body) {
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            if (Arrays.equals(part.getContentType(),
                            ContentType.APP_SMIL.getBytes())) {
                return part;
            }
        }
        return null;
    }
    private static SMILDocument validate(SMILDocument in) {
        return in;
    }
    private static SMILDocument getSmilDocument(PduPart smilPart) {
        try {
            byte[] data = smilPart.getData();
            if (data != null) {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Parsing SMIL document.");
                    Log.v(TAG, new String(data));
                }
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                SMILDocument document = new SmilXmlParser().parse(bais);
                return validate(document);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to parse SMIL document.", e);
        } catch (SAXException e) {
            Log.e(TAG, "Failed to parse SMIL document.", e);
        } catch (MmsException e) {
            Log.e(TAG, "Failed to parse SMIL document.", e);
        }
        return null;
    }
    public static SMILParElement addPar(SMILDocument document) {
        SMILParElement par = (SMILParElement) document.createElement("par");
        par.setDur(8.0f);
        document.getBody().appendChild(par);
        return par;
    }
    public static SMILMediaElement createMediaElement(
            String tag, SMILDocument document, String src) {
        SMILMediaElement mediaElement =
                (SMILMediaElement) document.createElement(tag);
        mediaElement.setSrc(escapeXML(src));
        return mediaElement;
    }
    static public String escapeXML(String str) {
        return str.replaceAll("&","&amp;")
                  .replaceAll("<", "&lt;")
                  .replaceAll(">", "&gt;")
                  .replaceAll("\"", "&quot;")
                  .replaceAll("'", "&apos;");
    }
    private static SMILDocument createSmilDocument(PduBody pb) {
        if (Config.LOGV) {
            Log.v(TAG, "Creating default SMIL document.");
        }
        SMILDocument document = new SmilDocumentImpl();
        SMILElement smil = (SMILElement) document.createElement("smil");
        smil.setAttribute("xmlns", "http:
        document.appendChild(smil);
        SMILElement head = (SMILElement) document.createElement("head");
        smil.appendChild(head);
        SMILLayoutElement layout = (SMILLayoutElement) document.createElement("layout");
        head.appendChild(layout);
        SMILElement body = (SMILElement) document.createElement("body");
        smil.appendChild(body);
        SMILParElement par = addPar(document);
        int partsNum = pb.getPartsNum();
        if (partsNum == 0) {
            return document;
        }
        boolean hasText = false;
        boolean hasMedia = false;
        for (int i = 0; i < partsNum; i++) {
            if ((par == null) || (hasMedia && hasText)) {
                par = addPar(document);
                hasText = false;
                hasMedia = false;
            }
            PduPart part = pb.getPart(i);
            String contentType = new String(part.getContentType());
            if (ContentType.isDrmType(contentType)) {
                DrmWrapper dw;
                try {
                    dw = new DrmWrapper(contentType, part.getDataUri(),
                                        part.getData());
                    contentType = dw.getContentType();
                } catch (DrmException e) {
                    Log.e(TAG, e.getMessage(), e);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            if (contentType.equals(ContentType.TEXT_PLAIN)
                    || contentType.equalsIgnoreCase(ContentType.APP_WAP_XHTML)
                    || contentType.equals(ContentType.TEXT_HTML)) {
                SMILMediaElement textElement = createMediaElement(
                        ELEMENT_TAG_TEXT, document, part.generateLocation());
                par.appendChild(textElement);
                hasText = true;
            } else if (ContentType.isImageType(contentType)) {
                SMILMediaElement imageElement = createMediaElement(
                        ELEMENT_TAG_IMAGE, document, part.generateLocation());
                par.appendChild(imageElement);
                hasMedia = true;
            } else if (ContentType.isVideoType(contentType)) {
                SMILMediaElement videoElement = createMediaElement(
                        ELEMENT_TAG_VIDEO, document, part.generateLocation());
                par.appendChild(videoElement);
                hasMedia = true;
            } else if (ContentType.isAudioType(contentType)) {
                SMILMediaElement audioElement = createMediaElement(
                        ELEMENT_TAG_AUDIO, document, part.generateLocation());
                par.appendChild(audioElement);
                hasMedia = true;
            } else {
                Log.w(TAG, "unsupport media type");
            }
        }
        return document;
    }
    private static SMILDocument createSmilDocument(SlideshowModel slideshow) {
        if (Config.LOGV) {
            Log.v(TAG, "Creating SMIL document from SlideshowModel.");
        }
        SMILDocument document = new SmilDocumentImpl();
        SMILElement smilElement = (SMILElement) document.createElement("smil");
        document.appendChild(smilElement);
        SMILElement headElement = (SMILElement) document.createElement("head");
        smilElement.appendChild(headElement);
        SMILLayoutElement layoutElement = (SMILLayoutElement)
                document.createElement("layout");
        headElement.appendChild(layoutElement);
        SMILRootLayoutElement rootLayoutElement =
                (SMILRootLayoutElement) document.createElement("root-layout");
        LayoutModel layouts = slideshow.getLayout();
        rootLayoutElement.setWidth(layouts.getLayoutWidth());
        rootLayoutElement.setHeight(layouts.getLayoutHeight());
        String bgColor = layouts.getBackgroundColor();
        if (!TextUtils.isEmpty(bgColor)) {
            rootLayoutElement.setBackgroundColor(bgColor);
        }
        layoutElement.appendChild(rootLayoutElement);
        ArrayList<RegionModel> regions = layouts.getRegions();
        ArrayList<SMILRegionElement> smilRegions = new ArrayList<SMILRegionElement>();
        for (RegionModel r : regions) {
            SMILRegionElement smilRegion = (SMILRegionElement) document.createElement("region");
            smilRegion.setId(r.getRegionId());
            smilRegion.setLeft(r.getLeft());
            smilRegion.setTop(r.getTop());
            smilRegion.setWidth(r.getWidth());
            smilRegion.setHeight(r.getHeight());
            smilRegion.setFit(r.getFit());
            smilRegions.add(smilRegion);
        }
        SMILElement bodyElement = (SMILElement) document.createElement("body");
        smilElement.appendChild(bodyElement);
        boolean txtRegionPresentInLayout = false;
        boolean imgRegionPresentInLayout = false;
        for (SlideModel slide : slideshow) {
            SMILParElement par = addPar(document);
            par.setDur(slide.getDuration() / 1000f);
            addParElementEventListeners((EventTarget) par, slide);
            for (MediaModel media : slide) {
                SMILMediaElement sme = null;
                String src = media.getSrc();
                if (media instanceof TextModel) {
                    TextModel text = (TextModel) media;
                    if (TextUtils.isEmpty(text.getText())) {
                        if (LOCAL_LOGV) {
                            Log.v(TAG, "Empty text part ignored: " + text.getSrc());
                        }
                        continue;
                    }
                    sme = SmilHelper.createMediaElement(SmilHelper.ELEMENT_TAG_TEXT, document, src);
                    txtRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme,
                                                         smilRegions,
                                                         layoutElement,
                                                         LayoutModel.TEXT_REGION_ID,
                                                         txtRegionPresentInLayout);
                } else if (media instanceof ImageModel) {
                    sme = SmilHelper.createMediaElement(SmilHelper.ELEMENT_TAG_IMAGE, document, src);
                    imgRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme,
                                                         smilRegions,
                                                         layoutElement,
                                                         LayoutModel.IMAGE_REGION_ID,
                                                         imgRegionPresentInLayout);
                } else if (media instanceof VideoModel) {
                    sme = SmilHelper.createMediaElement(SmilHelper.ELEMENT_TAG_VIDEO, document, src);
                    imgRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme,
                                                         smilRegions,
                                                         layoutElement,
                                                         LayoutModel.IMAGE_REGION_ID,
                                                         imgRegionPresentInLayout);
                } else if (media instanceof AudioModel) {
                    sme = SmilHelper.createMediaElement(SmilHelper.ELEMENT_TAG_AUDIO, document, src);
                } else {
                    Log.w(TAG, "Unsupport media: " + media);
                    continue;
                }
                int begin = media.getBegin();
                if (begin != 0) {
                    sme.setAttribute("begin", String.valueOf(begin / 1000));
                }
                int duration = media.getDuration();
                if (duration != 0) {
                    sme.setDur((float) duration / 1000);
                }
                par.appendChild(sme);
                addMediaElementEventListeners((EventTarget) sme, media);
            }
        }
        if (LOCAL_LOGV) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            SmilXmlSerializer.serialize(document, out);
            Log.v(TAG, out.toString());
        }
        return document;
    }
    private static SMILRegionElement findRegionElementById(
            ArrayList<SMILRegionElement> smilRegions, String rId) {
        for (SMILRegionElement smilRegion : smilRegions) {
            if (smilRegion.getId().equals(rId)) {
                return smilRegion;
            }
        }
        return null;
    }
    private static boolean setRegion(SMILRegionMediaElement srme,
                                     ArrayList<SMILRegionElement> smilRegions,
                                     SMILLayoutElement smilLayout,
                                     String regionId,
                                     boolean regionPresentInLayout) {
        SMILRegionElement smilRegion = findRegionElementById(smilRegions, regionId);
        if (!regionPresentInLayout && smilRegion != null) {
            srme.setRegion(smilRegion);
            smilLayout.appendChild(smilRegion);
            return true;
        }
        return false;
    }
    static void addMediaElementEventListeners(
            EventTarget target, MediaModel media) {
        target.addEventListener(SMIL_MEDIA_START_EVENT, media, false);
        target.addEventListener(SMIL_MEDIA_END_EVENT, media, false);
        target.addEventListener(SMIL_MEDIA_PAUSE_EVENT, media, false);
        target.addEventListener(SMIL_MEDIA_SEEK_EVENT, media, false);
    }
    static void addParElementEventListeners(
            EventTarget target, SlideModel slide) {
        target.addEventListener(SMIL_SLIDE_START_EVENT, slide, false);
        target.addEventListener(SMIL_SLIDE_END_EVENT, slide, false);
    }
}
