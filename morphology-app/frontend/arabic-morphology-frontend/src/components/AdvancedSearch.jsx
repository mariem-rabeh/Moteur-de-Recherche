import React, { useState, useEffect } from 'react';
import { Search, BookOpen, FileText, Loader, Hash } from 'lucide-react';
import api from '../services/api';

const AdvancedSearch = () => {
  /* ── shared ── */
  const [activeTab, setActiveTab] = useState('scheme');

  /* ── tab 1 : search by scheme ── */
  const [schemeQuery, setSchemeQuery]             = useState('');
  const [schemeResults, setSchemeResults]         = useState([]);
  const [schemeLoading, setSchemeLoading]         = useState(false);
  const [schemeError, setSchemeError]             = useState(null);
  const [schemeSearched, setSchemeSearched]       = useState(false);
  const [schemeFilter, setSchemeFilter]           = useState('');
  const [schemesSuggestions, setSchemesSuggestions] = useState([]);

  /* ── tab 2 : derivatives ── */
  const [rootQuery, setRootQuery]                 = useState('');
  const [derivativesData, setDerivativesData]     = useState(null);
  const [derivLoading, setDerivLoading]           = useState(false);
  const [derivError, setDerivError]               = useState(null);
  const [derivSearched, setDerivSearched]         = useState(false);
  const [sortBy, setSortBy]                       = useState('freq');
  const [rootSuggestions, setRootSuggestions]     = useState([]);

  /* ── load suggestions once ── */
  useEffect(() => {
    const load = async () => {
      try {
        const [rRes, sRes] = await Promise.all([
          api.getRoots('', 1, 1000),
          api.getSchemes(),
        ]);
        const rootsData   = rRes.data?.data?.roots || rRes.data?.roots || rRes.data || [];
        const schemesData = sRes.data?.data || sRes.data || [];
        setRootSuggestions(Array.isArray(rootsData)   ? rootsData   : []);
        setSchemesSuggestions(Array.isArray(schemesData) ? schemesData : []);
      } catch (_) { /* silent */ }
    };
    load();
  }, []);

  /* ================================================================
     FEATURE 1 — Search by scheme
     ================================================================ */
  const handleSchemeSearch = async () => {
    if (!schemeQuery.trim()) { setSchemeError('الرجاء إدخال الوزن الصرفي'); return; }
    setSchemeLoading(true);
    setSchemeError(null);
    setSchemeResults([]);
    setSchemeSearched(false);

    try {
      const res  = await api.searchByScheme(schemeQuery.trim());
      const data = res.data?.data || res.data || [];
      if (Array.isArray(data)) {
        setSchemeResults(data);
      } else {
        setSchemeError('تنسيق الاستجابة غير صحيح');
      }
    } catch (err) {
      setSchemeError(err.message || 'خطأ في البحث');
    } finally {
      setSchemeLoading(false);
      setSchemeSearched(true);
    }
  };

  /* ================================================================
     FEATURE 2 — Derivatives of a root
     ================================================================ */
  const handleDerivSearch = async () => {
    if (!rootQuery.trim()) { setDerivError('الرجاء إدخال الجذر'); return; }
    setDerivLoading(true);
    setDerivError(null);
    setDerivativesData(null);
    setDerivSearched(false);

    try {
      const res  = await api.getDerivatives(rootQuery.trim());
      const data = res.data?.data || res.data;
      if (data && data.root) {
        setDerivativesData(data);
      } else if (res.data?.success === false) {
        setDerivError(res.data?.message || 'جذر غير موجود');
      } else {
        setDerivError('تنسيق الاستجابة غير صحيح');
      }
    } catch (err) {
      setDerivError(err.message || 'خطأ في البحث');
    } finally {
      setDerivLoading(false);
      setDerivSearched(true);
    }
  };

  /* ── helpers ── */
  const filteredSchemeResults = schemeFilter
    ? schemeResults.filter(r => r.rootType === schemeFilter)
    : schemeResults;

  const uniqueRootTypes = [...new Set(schemeResults.map(r => r.rootType).filter(Boolean))];

  const sortedDerives = derivativesData?.derives
    ? [...derivativesData.derives].sort((a, b) =>
        sortBy === 'freq'
          ? b.frequence - a.frequence
          : a.mot.localeCompare(b.mot, 'ar')
      )
    : [];

  const maxFreq = sortedDerives.length > 0
    ? Math.max(...sortedDerives.map(d => d.frequence))
    : 1;

  /* ================================================================
     RENDER
     ================================================================ */
  return (
    <div className="page-container">

      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">←</span>
        <span className="active">البحث المتقدم</span>
      </div>

      {/* Page Header */}
      <div className="page-header">
        <div className="ornament-icon"><Search size={32} /></div>
        <div>
          <h1 className="page-title">البحث المتقدم</h1>
          <p style={{ color: '#666', marginTop: 4, fontFamily: 'var(--font-arabic)' }}>
            بحث بالوزن الصرفي وبحث في مشتقات الجذور
          </p>
        </div>
      </div>

      {/* ── TAB SWITCHER ── */}
      <div style={styles.tabBar}>
        <button
          style={{ ...styles.tabBtn, ...(activeTab === 'scheme' ? styles.tabBtnActive : {}) }}
          onClick={() => setActiveTab('scheme')}
        >
          <Hash size={18} />
          <span>البحث بالوزن الصرفي</span>
        </button>
        <button
          style={{ ...styles.tabBtn, ...(activeTab === 'derivatives' ? styles.tabBtnActive : {}) }}
          onClick={() => setActiveTab('derivatives')}
        >
          <BookOpen size={18} />
          <span>مشتقات الجذر</span>
        </button>
      </div>

      {/* ================================================================
          TAB 1 — Search by scheme
          ================================================================ */}
      {activeTab === 'scheme' && (
        <div>

          {/* ── Input card ── */}
          <div className="card">
            <div className="card-header">
              <Hash size={20} />
              <h3>البحث عن كل الكلمات المطابقة لوزن صرفي</h3>
            </div>
            <div className="card-body">
              <p style={styles.description}>
                أدخل وزناً صرفياً (مثال: <strong>فاعل</strong>، <strong>مفعول</strong>) لعرض جميع
                الكلمات المولّدة من هذا الوزن مع كل الجذور المتاحة في النظام.
              </p>

              <div style={styles.searchRow}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="أدخل الوزن الصرفي (مثال: فاعل)"
                  value={schemeQuery}
                  onChange={e => setSchemeQuery(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleSchemeSearch()}
                  list="schemes-suggest"
                  style={{ flex: 1, fontSize: '1.1rem' }}
                />
                <datalist id="schemes-suggest">
                  {schemesSuggestions.map((s, i) => (
                    <option key={i} value={s.name || s}>{s.rule || ''}</option>
                  ))}
                </datalist>
                <button
                  className="btn btn-primary btn-lg"
                  onClick={handleSchemeSearch}
                  disabled={schemeLoading}
                  style={{ minWidth: 130 }}
                >
                  {schemeLoading
                    ? <Loader className="spin" size={20} />
                    : <Search size={20} />}
                  بحث
                </button>
              </div>

              {/* ── خطأ في حقل الإدخال (قبل البحث) ── */}
              {schemeError && !schemeSearched && (
                <div className="alert alert-danger" style={{ marginTop: 16 }}>
                  <strong>خطأ:</strong> {schemeError}
                </div>
              )}
            </div>
          </div>

          {/* ── Results card — يظهر دائماً بعد البحث ── */}
          {schemeSearched && !schemeLoading && (
            <div className="card">
              <div className="card-header">
                <FileText size={20} />
                <h3>نتائج الوزن: {schemeQuery}</h3>
                {!schemeError && (
                  <span className="root-count">{filteredSchemeResults.length}</span>
                )}
              </div>
              <div className="card-body">

                {/* ══ خطأ يظهر داخل بلوك النتائج ══ */}
                {schemeError ? (
                  <div style={styles.errorBlock}>
                    <div style={styles.errorIcon}>✕</div>
                    <div>
                      <div style={styles.errorTitle}>تعذّر تنفيذ البحث</div>
                      <div style={styles.errorMsg}>{schemeError}</div>
                    </div>
                    <button
                      style={styles.retryBtn}
                      onClick={handleSchemeSearch}
                    >
                      إعادة المحاولة
                    </button>
                  </div>

                ) : schemeResults.length === 0 ? (
                  <div className="empty-state">
                    <Search size={48} />
                    <p>لا توجد نتائج لهذا الوزن</p>
                    <p className="empty-hint">تحقق من كتابة الوزن أو أضف جذوراً أولاً</p>
                  </div>

                ) : (
                  <>
                    {/* Filter bar */}
                    {uniqueRootTypes.length > 1 && (
                      <div style={styles.filterBar}>
                        <span style={styles.filterLabel}>تصفية حسب النوع:</span>
                        <button
                          style={{ ...styles.filterChip, ...(schemeFilter === '' ? styles.filterChipActive : {}) }}
                          onClick={() => setSchemeFilter('')}
                        >
                          الكل ({schemeResults.length})
                        </button>
                        {uniqueRootTypes.map(type => (
                          <button
                            key={type}
                            style={{ ...styles.filterChip, ...(schemeFilter === type ? styles.filterChipActive : {}) }}
                            onClick={() => setSchemeFilter(schemeFilter === type ? '' : type)}
                          >
                            {type} ({schemeResults.filter(r => r.rootType === type).length})
                          </button>
                        ))}
                      </div>
                    )}

                    {/* Word grid */}
                    <div className="family-grid" style={{ marginTop: 20 }}>
                      {filteredSchemeResults.map((item, i) => (
                        <div key={i} className="family-item" style={styles.schemeCard}>
                          <div className="family-word" style={{ fontSize: '1.8rem' }}>{item.word}</div>
                          <div style={styles.schemeCardMeta}>
                            <span style={styles.badge}>{item.root}</span>
                            {item.rootType && (
                              <span style={{ ...styles.badge, background: '#e8f4f8', color: '#2d6a6b' }}>
                                {item.rootType}
                              </span>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Summary */}
                    <div className="family-stats">
                      إجمالي الكلمات المولّدة بوزن «{schemeQuery}»:{' '}
                      <strong>{filteredSchemeResults.length}</strong>
                    </div>
                  </>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ================================================================
          TAB 2 — Derivatives of a root
          ================================================================ */}
      {activeTab === 'derivatives' && (
        <div>

          {/* ── Input card ── */}
          <div className="card">
            <div className="card-header">
              <BookOpen size={20} />
              <h3>البحث عن مشتقات جذر</h3>
            </div>
            <div className="card-body">
              <p style={styles.description}>
                أدخل جذراً ثلاثياً (مثال: <strong>كتب</strong>، <strong>درس</strong>) لعرض جميع
                الكلمات المشتقة منه المسجّلة في النظام مع تكرار استخدام كل منها.
              </p>

              <div style={styles.searchRow}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="أدخل الجذر (مثال: كتب)"
                  value={rootQuery}
                  onChange={e => setRootQuery(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleDerivSearch()}
                  list="roots-suggest"
                  style={{ flex: 1, fontSize: '1.1rem' }}
                  maxLength={3}
                />
                <datalist id="roots-suggest">
                  {rootSuggestions.map((r, i) => (
                    <option key={i} value={r} />
                  ))}
                </datalist>
                <button
                  className="btn btn-primary btn-lg"
                  onClick={handleDerivSearch}
                  disabled={derivLoading}
                  style={{ minWidth: 130 }}
                >
                  {derivLoading
                    ? <Loader className="spin" size={20} />
                    : <Search size={20} />}
                  بحث
                </button>
              </div>

              {/* خطأ قبل البحث (مثل حقل فارغ) */}
              {derivError && !derivSearched && (
                <div className="alert alert-danger" style={{ marginTop: 16 }}>
                  <strong>خطأ:</strong> {derivError}
                </div>
              )}
            </div>
          </div>

          {/* ── Results card ── */}
          {derivSearched && !derivLoading && (
            <div className="card">
              <div className="card-header">
                <BookOpen size={20} />
                <h3>
                  {derivativesData
                    ? `مشتقات الجذر: ${derivativesData.root}`
                    : `نتائج البحث عن: ${rootQuery}`}
                </h3>
                {derivativesData && (
                  <span className="root-count">{derivativesData.nombreDerives}</span>
                )}
              </div>
              <div className="card-body">

                {/* ══ خطأ يظهر داخل بلوك النتائج ══ */}
                {derivError ? (
                  <div style={styles.errorBlock}>
                    <div style={styles.errorIcon}>✕</div>
                    <div>
                      <div style={styles.errorTitle}>تعذّر تنفيذ البحث</div>
                      <div style={styles.errorMsg}>{derivError}</div>
                    </div>
                    <button style={styles.retryBtn} onClick={handleDerivSearch}>
                      إعادة المحاولة
                    </button>
                  </div>

                ) : derivativesData ? (
                  <>
                    {/* Root info bar */}
                    <div style={styles.rootInfoBar}>
                      <div style={styles.rootInfoItem}>
                        <span style={styles.rootInfoLabel}>الجذر</span>
                        <span style={{ ...styles.rootInfoValue, fontFamily: 'var(--font-arabic)', fontSize: '1.6rem' }}>
                          {derivativesData.root}
                        </span>
                      </div>
                      <div style={styles.rootInfoItem}>
                        <span style={styles.rootInfoLabel}>النوع الصرفي</span>
                        <span style={styles.rootInfoValue}>{derivativesData.rootType}</span>
                      </div>
                      <div style={styles.rootInfoItem}>
                        <span style={styles.rootInfoLabel}>عدد المشتقات</span>
                        <span style={{ ...styles.rootInfoValue, color: C.cream, fontWeight: 700 }}>
                          {derivativesData.nombreDerives}
                        </span>
                      </div>
                      <div style={styles.rootInfoItem}>
                        <span style={styles.rootInfoLabel}>مجموع التكرار</span>
                        <span style={styles.rootInfoValue}>{derivativesData.frequenceRacine}</span>
                      </div>
                    </div>

                    {derivativesData.nombreDerives === 0 ? (
                      <div className="empty-state" style={{ marginTop: 30 }}>
                        <BookOpen size={48} />
                        <p>لا توجد مشتقات مسجّلة لهذا الجذر بعد</p>
                        <p className="empty-hint">
                          استخدم وظيفة التوليد الصرفي لإنشاء كلمات من هذا الجذر
                        </p>
                      </div>
                    ) : (
                      <>
                        {/* Sort controls */}
                        <div style={styles.sortBar}>
                          <span style={styles.filterLabel}>ترتيب:</span>
                          <button
                            style={{ ...styles.filterChip, ...(sortBy === 'freq' ? styles.filterChipActive : {}) }}
                            onClick={() => setSortBy('freq')}
                          >حسب التكرار</button>
                          <button
                            style={{ ...styles.filterChip, ...(sortBy === 'alpha' ? styles.filterChipActive : {}) }}
                            onClick={() => setSortBy('alpha')}
                          >أبجدياً</button>
                        </div>

                        {/* Derivatives list */}
                        <div style={{ marginTop: 20, display: 'flex', flexDirection: 'column', gap: 10 }}>
                          {sortedDerives.map((d, i) => (
                            <div key={i} style={styles.derivRow}>
                              <span style={styles.derivRank}>{i + 1}</span>
                              <span style={styles.derivWord}>{d.mot}</span>
                              <div style={styles.derivBarWrapper}>
                                <div style={{
                                  ...styles.derivBar,
                                  width: `${Math.max(4, (d.frequence / maxFreq) * 100)}%`,
                                }} />
                              </div>
                              <span style={styles.derivFreq}>×{d.frequence}</span>
                            </div>
                          ))}
                        </div>

                        <div className="family-stats">
                          إجمالي المشتقات: <strong>{derivativesData.nombreDerives}</strong>
                          &nbsp;|&nbsp;
                          مجموع التكرار: <strong>{derivativesData.frequenceRacine}</strong>
                        </div>
                      </>
                    )}
                  </>
                ) : (
                  <div className="empty-state">
                    <BookOpen size={48} />
                    <p>لم يتم العثور على الجذر</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

/* ── Colors ── */
const C = {
  teal:       '#2d6a6b',
  dark:       '#0d3b3f',
  gold:       '#d4a574',
  goldBorder: '#b8935f',
  cream:      '#f5e6d3',
  green:      '#4a7c59',
};

const styles = {
  tabBar: {
    display: 'flex', gap: 8, marginBottom: 24,
    background: '#f0f0f0', padding: 6, borderRadius: 12, border: '1px solid #ddd',
  },
  tabBtn: {
    flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
    gap: 8, padding: '12px 20px', border: 'none', borderRadius: 8,
    background: 'transparent', color: '#666', fontFamily: 'var(--font-body)',
    fontSize: '1rem', cursor: 'pointer', fontWeight: 600, transition: 'all 0.25s',
  },
  tabBtnActive: {
    background: `linear-gradient(90deg, ${C.teal} 0%, ${'#0d3b3f'} 100%)`,
    color: '#fff', boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
  },
  description: {
    fontFamily: 'var(--font-arabic)', color: '#555',
    marginBottom: 20, lineHeight: 1.8, fontSize: '1rem',
  },
  searchRow: { display: 'flex', gap: 12, alignItems: 'center' },

  /* ── Error block inside results card ── */
  errorBlock: {
    display: 'flex', alignItems: 'center', gap: 16,
    padding: '20px 24px', borderRadius: 10,
    background: '#fff5f5', border: '1px solid #fca5a5',
  },
  errorIcon: {
    width: 40, height: 40, borderRadius: '50%',
    background: '#fee2e2', color: '#dc2626',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: '1.2rem', fontWeight: 700, flexShrink: 0,
  },
  errorTitle: {
    fontWeight: 700, color: '#991b1b',
    fontFamily: 'var(--font-arabic)', marginBottom: 4,
  },
  errorMsg: { color: '#b91c1c', fontFamily: 'var(--font-arabic)', fontSize: '0.95rem' },
  retryBtn: {
    marginRight: 'auto', padding: '8px 18px', borderRadius: 8,
    border: '1px solid #dc2626', background: 'white', color: '#dc2626',
    cursor: 'pointer', fontFamily: 'var(--font-body)', fontWeight: 600,
    fontSize: '0.9rem', transition: 'all 0.2s', flexShrink: 0,
  },

  filterBar: {
    display: 'flex', flexWrap: 'wrap', gap: 8, alignItems: 'center',
    marginTop: 16, padding: '12px 16px', background: '#f8f9fa',
    borderRadius: 8, border: '1px solid #e0e0e0',
  },
  filterLabel: { fontWeight: 600, color: '#555', fontSize: '0.9rem' },
  filterChip: {
    padding: '5px 14px', borderRadius: 20, border: `1px solid ${'#b8935f'}`,
    background: 'white', color: C.dark, cursor: 'pointer',
    fontFamily: 'var(--font-body)', fontSize: '0.85rem',
    transition: 'all 0.2s', fontWeight: 500,
  },
  filterChipActive: {
    background: C.gold, color: C.dark, borderColor: C.goldBorder,
    boxShadow: '0 2px 6px rgba(0,0,0,0.15)',
  },
  schemeCard: {
    display: 'flex', flexDirection: 'column', alignItems: 'center',
    gap: 10, padding: '18px 12px', borderRadius: 10,
    border: '1px solid #e0e0e0', background: 'white', transition: 'all 0.25s',
  },
  schemeCardMeta: { display: 'flex', gap: 6, flexWrap: 'wrap', justifyContent: 'center' },
  badge: {
    padding: '2px 10px', borderRadius: 12, background: C.cream, color: C.dark,
    fontSize: '0.8rem', fontFamily: 'var(--font-arabic)', fontWeight: 600,
    border: `1px solid ${C.goldBorder}`,
  },
  rootInfoBar: {
    display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: 12, marginBottom: 24, padding: '16px 20px',
    background: `linear-gradient(135deg, ${C.dark} 0%, ${C.teal} 100%)`,
    borderRadius: 10, color: 'white',
  },
  rootInfoItem:  { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4 },
  rootInfoLabel: { fontSize: '0.78rem', opacity: 0.75, fontWeight: 500 },
  rootInfoValue: { fontSize: '1.2rem', fontWeight: 700, color: C.cream },
  sortBar:       { display: 'flex', gap: 8, alignItems: 'center', marginTop: 8 },
  derivRow: {
    display: 'flex', alignItems: 'center', gap: 14,
    padding: '12px 16px', background: '#f8f9fa',
    borderRadius: 8, border: '1px solid #e8e8e8', transition: 'all 0.2s',
  },
  derivRank: {
    width: 28, height: 28, borderRadius: '50%',
    background: `linear-gradient(135deg, ${C.gold} 0%, ${C.goldBorder} 100%)`,
    color: C.dark, display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: '0.8rem', fontWeight: 700, flexShrink: 0,
  },
  derivWord:       { fontFamily: 'var(--font-arabic)', fontSize: '1.3rem', fontWeight: 600, color: C.dark, minWidth: 90 },
  derivBarWrapper: { flex: 1, height: 10, background: '#e0e0e0', borderRadius: 5, overflow: 'hidden' },
  derivBar: {
    height: '100%',
    background: `linear-gradient(90deg, ${C.teal} 0%, ${'#4a7c59'} 100%)`,
    borderRadius: 5, transition: 'width 0.5s ease',
  },
  derivFreq: { minWidth: 40, textAlign: 'left', fontWeight: 700, color: C.teal, fontSize: '1rem' },
};

export default AdvancedSearch;  