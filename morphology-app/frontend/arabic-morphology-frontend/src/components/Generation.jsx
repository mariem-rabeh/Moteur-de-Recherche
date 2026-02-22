import React, { useState, useEffect } from 'react';
import { Wand2, FileText, Loader, Search } from 'lucide-react';
import api from '../services/api';

const Generation = () => {
  const [roots, setRoots] = useState([]);
  const [schemes, setSchemes] = useState([]);

  // ── bloc 1 : mot unique
  const [selectedRoot, setSelectedRoot] = useState('');
  const [selectedScheme, setSelectedScheme] = useState('');
  const [generatedWord, setGeneratedWord] = useState(null);
  const [wordError, setWordError] = useState(null);

  // ── bloc 2 : famille
  const [generatedFamily, setGeneratedFamily] = useState([]);
  const [familyError, setFamilyError] = useState(null);

  // ── bloc 3 : par wazn
  const [schemeForAll, setSchemeForAll] = useState('');
  const [schemeResults, setSchemeResults] = useState([]);
  const [schemeError, setSchemeError] = useState(null);

  // ── global
  const [loading, setLoading] = useState(false);
  const [dataLoading, setDataLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setDataLoading(true);
    try {
      const [rootsRes, schemesRes] = await Promise.all([
        api.getRoots('', 1, 1000),
        api.getSchemes()
      ]);

      const rootsData = rootsRes.data?.data?.roots
        || rootsRes.data?.roots
        || rootsRes.data?.data
        || rootsRes.data
        || [];

      const schemesData = schemesRes.data?.data
        || schemesRes.data
        || [];

      setRoots(Array.isArray(rootsData) ? rootsData : []);
      setSchemes(Array.isArray(schemesData) ? schemesData : []);
    } catch (error) {
      console.error('Error loading data:', error);
      setRoots([]);
      setSchemes([]);
    } finally {
      setDataLoading(false);
    }
  };

  // ================================================================
  // Bloc 1 — توليد كلمة واحدة
  // ================================================================
  const handleGenerateWord = async () => {
    if (!selectedRoot || !selectedScheme) {
      alert('الرجاء إدخال الجذر والوزن');
      return;
    }
    setLoading(true);
    setWordError(null);
    setGeneratedWord(null);
    try {
      const response = await api.generateWord(selectedRoot.trim(), selectedScheme.trim());
      let wordData = null;
      if (response.data?.success === true) {
        wordData = response.data.data;
      } else if (response.data?.success === false) {
        setWordError(response.data?.message || 'فشل توليد الكلمة');
        return;
      } else if (response.data?.word) {
        wordData = response.data;
      } else if (response.data?.data) {
        wordData = response.data.data;
      }
      if (wordData && wordData.word) {
        setGeneratedWord(wordData);
      } else {
        setWordError('تنسيق الاستجابة غير صحيح - لا توجد كلمة في الاستجابة');
      }
    } catch (error) {
      setWordError(error.response?.data?.message || error.message || 'خطأ في توليد الكلمة');
    } finally {
      setLoading(false);
    }
  };

  // ================================================================
  // Bloc 2 — توليد العائلة الصرفية
  // ================================================================
  const handleGenerateFamily = async () => {
    if (!selectedRoot) {
      alert('الرجاء إدخال الجذر');
      return;
    }
    setLoading(true);
    setFamilyError(null);
    setGeneratedFamily([]);
    try {
      const response = await api.generateFamily(selectedRoot.trim());
      let familyData = null;
      if (response.data?.success === true) {
        familyData = response.data.data;
      } else if (response.data?.success === false) {
        setFamilyError(response.data?.message || 'فشل توليد العائلة');
        return;
      } else if (Array.isArray(response.data)) {
        familyData = response.data;
      } else if (response.data?.data && Array.isArray(response.data.data)) {
        familyData = response.data.data;
      }
      if (familyData && Array.isArray(familyData) && familyData.length > 0) {
        setGeneratedFamily(familyData);
      } else {
        setFamilyError('لا توجد عائلة صرفية لهذا الجذر');
      }
    } catch (error) {
      setFamilyError(error.response?.data?.message || error.message || 'خطأ في توليد العائلة الصرفية');
    } finally {
      setLoading(false);
    }
  };

  // ================================================================
  // Bloc 3 — توليد كل الكلمات بوزن معين
  // ================================================================
  const handleGenerateByScheme = async () => {
    if (!schemeForAll) {
      alert('الرجاء إدخال الوزن');
      return;
    }
    setLoading(true);
    setSchemeError(null);
    setSchemeResults([]);
    try {
      const res = await api.generateByScheme(schemeForAll);

      if (res.data?.success === false) {
        setSchemeError(res.data?.message || `الوزن "${schemeForAll}" غير موجود في النظام`);
        return;
      }

      const data = res.data?.data || res.data || [];
      const filtered = Array.isArray(data) ? data.filter(i => i.success && i.word) : [];

      if (filtered.length === 0) {
        setSchemeError(`لا توجد كلمات مولّدة بالوزن "${schemeForAll}"`);
      } else {
        setSchemeResults(filtered);
      }
    } catch (err) {
      setSchemeError(err.response?.data?.message || err.message || 'خطأ في البحث');
    } finally {
      setLoading(false);
    }
  };

  // ================================================================
  // Loading screen
  // ================================================================
  if (dataLoading) {
    return (
      <div className="page-container">
        <div className="loading">
          <Loader className="spin" size={40} />
          <p>جاري تحميل البيانات...</p>
        </div>
      </div>
    );
  }

  // ================================================================
  // Render
  // ================================================================
  return (
    <div className="page-container">

      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">←</span>
        <span>التوليد الصرفي</span>
      </div>

      {/* Page Header */}
      <div className="page-header">
        <div className="ornament-icon">
          <Wand2 size={32} />
        </div>
        <h2 className="page-title">التوليد الصرفي</h2>
      </div>

      {/* ── 1. توليد كلمة واحدة ── */}
      <div className="card">
        <div className="card-header">
          <Wand2 size={20} />
          <h3>توليد كلمة واحدة</h3>
        </div>
        <div className="card-body">
          <div className="generation-form">
            <div className="form-group">
              <label>اختر الجذر:</label>
              <input
                type="text"
                value={selectedRoot}
                onChange={(e) => setSelectedRoot(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الجذر (مثال: كتب)"
                list="roots-list"
              />
              <datalist id="roots-list">
                {roots.map((root, index) => (
                  <option key={index} value={root} />
                ))}
              </datalist>
            </div>

            <div className="form-group">
              <label>اختر الوزن:</label>
              <input
                type="text"
                value={selectedScheme}
                onChange={(e) => setSelectedScheme(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الوزن (مثال: فاعل)"
                list="schemes-list"
              />
              <datalist id="schemes-list">
                {schemes.map((scheme, index) => (
                  <option key={index} value={scheme.name}>
                    {scheme.name} - {scheme.rule}
                  </option>
                ))}
              </datalist>
            </div>

            <button
              className="btn btn-primary btn-lg"
              onClick={handleGenerateWord}
              disabled={loading}
            >
              {loading ? <Loader className="spin" size={20} /> : <Wand2 size={20} />}
              توليد الكلمة
            </button>
          </div>

          {/* Erreur locale bloc 1 */}
          {wordError && (
            <div className="result-box error" style={{ marginTop: 16 }}>
              <h4>✗ خطأ</h4>
              <p>{wordError}</p>
            </div>
          )}

          {/* Résultat bloc 1 */}
          {generatedWord && generatedWord.word && (
            <div className="result-box success" style={{ marginTop: 16 }}>
              <h4>✓ النتيجة</h4>
              <div className="generated-word">
                <div className="word-display">{generatedWord.word}</div>
                <div className="word-details">
                  <span>الجذر: {generatedWord.root || selectedRoot}</span>
                  <span>الوزن: {generatedWord.scheme || selectedScheme}</span>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* ── 2. توليد العائلة الصرفية ── */}
      <div className="card">
        <div className="card-header">
          <FileText size={20} />
          <h3>توليد العائلة الصرفية الكاملة</h3>
        </div>
        <div className="card-body">
          <div className="generation-form">
            <div className="form-group">
              <label>اختر الجذر:</label>
              <input
                type="text"
                value={selectedRoot}
                onChange={(e) => setSelectedRoot(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الجذر (مثال: كتب)"
                list="roots-list-family"
              />
              <datalist id="roots-list-family">
                {roots.map((root, index) => (
                  <option key={index} value={root} />
                ))}
              </datalist>
            </div>

            <button
              className="btn btn-primary btn-lg"
              onClick={handleGenerateFamily}
              disabled={loading}
            >
              {loading ? <Loader className="spin" size={20} /> : <FileText size={20} />}
              توليد العائلة الكاملة
            </button>
          </div>

          {/* Erreur locale bloc 2 */}
          {familyError && (
            <div className="result-box error" style={{ marginTop: 16 }}>
              <h4>✗ خطأ</h4>
              <p>{familyError}</p>
            </div>
          )}

          {/* Résultat bloc 2 */}
          {generatedFamily.length > 0 && (
            <div className="result-box" style={{ marginTop: 16 }}>
              <h4>العائلة الصرفية للجذر "{selectedRoot}"</h4>
              <div className="family-grid">
                {generatedFamily
                  .filter(item => item.success && item.word)
                  .map((item, index) => (
                    <div key={index} className="family-item">
                      <div className="family-word">{item.word}</div>
                      <div className="family-scheme">{item.scheme || ''}</div>
                    </div>
                  ))
                }
              </div>
              <div className="family-stats">
                عدد الكلمات المولّدة: {generatedFamily.filter(i => i.success && i.word).length}
                {' / '}
                {generatedFamily.length}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* ── 3. توليد كل الكلمات بوزن معين ── */}
      <div className="card">
        <div className="card-header">
          <Search size={20} />
          <h3>توليد كل الكلمات بوزن معين</h3>
        </div>
        <div className="card-body">
          <div className="generation-form">
            <div className="form-group">
              <label>اختر الوزن:</label>
              <input
                type="text"
                value={schemeForAll}
                onChange={(e) => setSchemeForAll(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الوزن (مثال: فاعل)"
                list="schemes-list-all"
              />
              <datalist id="schemes-list-all">
                {schemes.map((scheme, index) => (
                  <option key={index} value={scheme.name} />
                ))}
              </datalist>
            </div>

            <button
              className="btn btn-primary btn-lg"
              onClick={handleGenerateByScheme}
              disabled={loading}
            >
              {loading ? <Loader className="spin" size={20} /> : <Search size={20} />}
              توليد بالوزن
            </button>
          </div>

          {/* Erreur locale bloc 3 */}
          {schemeError && (
            <div className="result-box error" style={{ marginTop: 16 }}>
              <h4>✗ خطأ</h4>
              <p>{schemeError}</p>
            </div>
          )}

          {/* Résultat bloc 3 */}
          {schemeResults.length > 0 && (
            <div className="result-box" style={{ marginTop: 16 }}>
              <h4>نتائج الوزن: {schemeForAll}</h4>
              <div className="family-grid">
                {schemeResults.map((item, index) => (
                  <div key={index} className="family-item">
                    <div className="family-word">{item.word}</div>
                    <div className="family-scheme">{item.root} — {item.scheme}</div>
                  </div>
                ))}
              </div>
              <div className="family-stats">
                عدد الكلمات: {schemeResults.length}
              </div>
            </div>
          )}
        </div>
      </div>

    </div>
  );
};

export default Generation;