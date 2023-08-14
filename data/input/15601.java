public class TrieIterator implements RangeValueIterator
{
    public TrieIterator(Trie trie)
    {
        if (trie == null) {
            throw new IllegalArgumentException(
                                          "Argument trie cannot be null");
        }
        m_trie_             = trie;
        m_initialValue_     = extract(m_trie_.getInitialValue());
        reset();
    }
    public final boolean next(Element element)
    {
        if (m_nextCodepoint_ > UCharacter.MAX_VALUE) {
            return false;
        }
        if (m_nextCodepoint_ < UCharacter.SUPPLEMENTARY_MIN_VALUE &&
            calculateNextBMPElement(element)) {
            return true;
        }
        calculateNextSupplementaryElement(element);
        return true;
    }
    public final void reset()
    {
        m_currentCodepoint_ = 0;
        m_nextCodepoint_    = 0;
        m_nextIndex_        = 0;
        m_nextBlock_ = m_trie_.m_index_[0] << Trie.INDEX_STAGE_2_SHIFT_;
        if (m_nextBlock_ == 0) {
            m_nextValue_ = m_initialValue_;
        }
        else {
            m_nextValue_ = extract(m_trie_.getValue(m_nextBlock_));
        }
        m_nextBlockIndex_ = 0;
        m_nextTrailIndexOffset_ = TRAIL_SURROGATE_INDEX_BLOCK_LENGTH_;
    }
    protected int extract(int value)
    {
        return value;
    }
    private final void setResult(Element element, int start, int limit,
                                 int value)
    {
        element.start = start;
        element.limit = limit;
        element.value = value;
    }
    private final boolean calculateNextBMPElement(Element element)
    {
        int currentBlock    = m_nextBlock_;
        int currentValue    = m_nextValue_;
        m_currentCodepoint_ = m_nextCodepoint_;
        m_nextCodepoint_ ++;
        m_nextBlockIndex_ ++;
        if (!checkBlockDetail(currentValue)) {
            setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                      currentValue);
            return true;
        }
        while (m_nextCodepoint_ < UCharacter.SUPPLEMENTARY_MIN_VALUE) {
            m_nextIndex_ ++;
            if (m_nextCodepoint_ == LEAD_SURROGATE_MIN_VALUE_) {
                m_nextIndex_ = BMP_INDEX_LENGTH_;
            }
            else if (m_nextCodepoint_ == TRAIL_SURROGATE_MIN_VALUE_) {
                m_nextIndex_ = m_nextCodepoint_ >> Trie.INDEX_STAGE_1_SHIFT_;
            }
            m_nextBlockIndex_ = 0;
            if (!checkBlock(currentBlock, currentValue)) {
                setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                          currentValue);
                return true;
            }
        }
        m_nextCodepoint_ --;   
        m_nextBlockIndex_ --;  
        return false;
    }
    private final void calculateNextSupplementaryElement(Element element)
    {
        int currentValue = m_nextValue_;
        int currentBlock = m_nextBlock_;
        m_nextCodepoint_ ++;
        m_nextBlockIndex_ ++;
        if (UTF16.getTrailSurrogate(m_nextCodepoint_)
                                        != UTF16.TRAIL_SURROGATE_MIN_VALUE) {
            if (!checkNullNextTrailIndex() && !checkBlockDetail(currentValue)) {
                setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                          currentValue);
                m_currentCodepoint_ = m_nextCodepoint_;
                return;
            }
            m_nextIndex_ ++;
            m_nextTrailIndexOffset_ ++;
            if (!checkTrailBlock(currentBlock, currentValue)) {
                setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                          currentValue);
                m_currentCodepoint_ = m_nextCodepoint_;
                return;
            }
        }
        int nextLead  = UTF16.getLeadSurrogate(m_nextCodepoint_);
        while (nextLead < TRAIL_SURROGATE_MIN_VALUE_) {
            int leadBlock =
                   m_trie_.m_index_[nextLead >> Trie.INDEX_STAGE_1_SHIFT_] <<
                                                   Trie.INDEX_STAGE_2_SHIFT_;
            if (leadBlock == m_trie_.m_dataOffset_) {
                if (currentValue != m_initialValue_) {
                    m_nextValue_      = m_initialValue_;
                    m_nextBlock_      = 0;
                    m_nextBlockIndex_ = 0;
                    setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                              currentValue);
                    m_currentCodepoint_ = m_nextCodepoint_;
                    return;
                }
                nextLead += DATA_BLOCK_LENGTH_;
                m_nextCodepoint_ = UCharacterProperty.getRawSupplementary(
                                     (char)nextLead,
                                     (char)UTF16.TRAIL_SURROGATE_MIN_VALUE);
                continue;
            }
            if (m_trie_.m_dataManipulate_ == null) {
                throw new NullPointerException(
                            "The field DataManipulate in this Trie is null");
            }
            m_nextIndex_ = m_trie_.m_dataManipulate_.getFoldingOffset(
                               m_trie_.getValue(leadBlock +
                                   (nextLead & Trie.INDEX_STAGE_3_MASK_)));
            if (m_nextIndex_ <= 0) {
                if (currentValue != m_initialValue_) {
                    m_nextValue_      = m_initialValue_;
                    m_nextBlock_      = 0;
                    m_nextBlockIndex_ = 0;
                    setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                              currentValue);
                    m_currentCodepoint_ = m_nextCodepoint_;
                    return;
                }
                m_nextCodepoint_ += TRAIL_SURROGATE_COUNT_;
            } else {
                m_nextTrailIndexOffset_ = 0;
                if (!checkTrailBlock(currentBlock, currentValue)) {
                    setResult(element, m_currentCodepoint_, m_nextCodepoint_,
                              currentValue);
                    m_currentCodepoint_ = m_nextCodepoint_;
                    return;
                }
            }
            nextLead ++;
         }
         setResult(element, m_currentCodepoint_, UCharacter.MAX_VALUE + 1,
                   currentValue);
    }
    private final boolean checkBlockDetail(int currentValue)
    {
        while (m_nextBlockIndex_ < DATA_BLOCK_LENGTH_) {
            m_nextValue_ = extract(m_trie_.getValue(m_nextBlock_ +
                                                    m_nextBlockIndex_));
            if (m_nextValue_ != currentValue) {
                return false;
            }
            ++ m_nextBlockIndex_;
            ++ m_nextCodepoint_;
        }
        return true;
    }
    private final boolean checkBlock(int currentBlock, int currentValue)
    {
        m_nextBlock_ = m_trie_.m_index_[m_nextIndex_] <<
                                                  Trie.INDEX_STAGE_2_SHIFT_;
        if (m_nextBlock_ == currentBlock &&
            (m_nextCodepoint_ - m_currentCodepoint_) >= DATA_BLOCK_LENGTH_) {
            m_nextCodepoint_ += DATA_BLOCK_LENGTH_;
        }
        else if (m_nextBlock_ == 0) {
            if (currentValue != m_initialValue_) {
                m_nextValue_      = m_initialValue_;
                m_nextBlockIndex_ = 0;
                return false;
            }
            m_nextCodepoint_ += DATA_BLOCK_LENGTH_;
        }
        else {
            if (!checkBlockDetail(currentValue)) {
                return false;
            }
        }
        return true;
    }
    private final boolean checkTrailBlock(int currentBlock,
                                          int currentValue)
    {
        while (m_nextTrailIndexOffset_ < TRAIL_SURROGATE_INDEX_BLOCK_LENGTH_)
        {
            m_nextBlockIndex_ = 0;
            if (!checkBlock(currentBlock, currentValue)) {
                return false;
            }
            m_nextTrailIndexOffset_ ++;
            m_nextIndex_ ++;
        }
        return true;
    }
    private final boolean checkNullNextTrailIndex()
    {
        if (m_nextIndex_ <= 0) {
            m_nextCodepoint_ += TRAIL_SURROGATE_COUNT_ - 1;
            int nextLead  = UTF16.getLeadSurrogate(m_nextCodepoint_);
            int leadBlock =
                   m_trie_.m_index_[nextLead >> Trie.INDEX_STAGE_1_SHIFT_] <<
                                                   Trie.INDEX_STAGE_2_SHIFT_;
            if (m_trie_.m_dataManipulate_ == null) {
                throw new NullPointerException(
                            "The field DataManipulate in this Trie is null");
            }
            m_nextIndex_ = m_trie_.m_dataManipulate_.getFoldingOffset(
                               m_trie_.getValue(leadBlock +
                                   (nextLead & Trie.INDEX_STAGE_3_MASK_)));
            m_nextIndex_ --;
            m_nextBlockIndex_ =  DATA_BLOCK_LENGTH_;
            return true;
        }
        return false;
    }
    private static final int BMP_INDEX_LENGTH_ =
                                        0x10000 >> Trie.INDEX_STAGE_1_SHIFT_;
    private static final int LEAD_SURROGATE_MIN_VALUE_ = 0xD800;
    private static final int TRAIL_SURROGATE_MIN_VALUE_ = 0xDC00;
    private static final int TRAIL_SURROGATE_COUNT_ = 0x400;
    private static final int TRAIL_SURROGATE_INDEX_BLOCK_LENGTH_ =
                                    1 << (10 - Trie.INDEX_STAGE_1_SHIFT_);
    private static final int DATA_BLOCK_LENGTH_ =
                                              1 << Trie.INDEX_STAGE_1_SHIFT_;
    private Trie m_trie_;
    private int m_initialValue_;
    private int m_currentCodepoint_;
    private int m_nextCodepoint_;
    private int m_nextValue_;
    private int m_nextIndex_;
    private int m_nextBlock_;
    private int m_nextBlockIndex_;
    private int m_nextTrailIndexOffset_;
}
