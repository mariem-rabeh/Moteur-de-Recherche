import React, { useState, useEffect } from 'react';
import { Wand2, FileText, Loader } from 'lucide-react';
import api from '../services/api';

const Generation = () => {
  const [roots, setRoots] = useState([]);
  const [schemes, setSchemes] = useState([]);
  const [selectedRoot, setSelectedRoot] = useState('');
  const [selectedScheme, setSelectedScheme] = useState('');
  const [generatedWord, setGeneratedWord] = useState(null);
  const [generatedFamily, setGeneratedFamily] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dataLoading, setDataLoading] = useState(true);
  const [error, setError] = useState(null);

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
      
      console.log('Roots Response:', rootsRes);
      console.log('Schemes Response:', schemesRes);
      
      const rootsData = rootsRes.data?.roots || rootsRes.data || [];
      const schemesData = schemesRes.data || [];
      
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

  const handleGenerateWord = async () => {
    if (!selectedRoot || !selectedScheme) {
      alert('الرجاء إدخال الجذر والوزن');
      return;
    }

    setLoading(true);
    setError(null);
    
    console.log('Generating word with:', { 
      root: selectedRoot.trim(), 
      scheme: selectedScheme.trim() 
    });
    
    try {
      const response = await api.generateWord(
        selectedRoot.trim(), 
        selectedScheme.trim()
      );
      
      console.log('Generate Word Response:', response);
      console.log('Response Data:', response.data);
      
      // La structure de réponse du backend est : 
      // { success: true, message: 'Succès', data: { word: '...', root: '...', scheme: '...' } }
      
      let wordData = null;
      
      // Vérifier si response.data a une structure ApiResponse (avec success)
      if (response.data && response.data.success === true) {
        // Extraire les données du wrapper ApiResponse
        wordData = response.data.data;
      } 
      // Sinon, vérifier les autres formats possibles
      else if (response.data) {
        if (response.data.word) {
          wordData = response.data;
        } else if (response.data.result) {
          wordData = response.data.result;
        } else if (response.data.data) {
          wordData = response.data.data;
        }
      }
      
      console.log('Processed Word Data:', wordData);
      console.log('Word Data keys:', wordData ? Object.keys(wordData) : 'null');
      
      if (wordData && wordData.word) {
        setGeneratedWord(wordData);
        setError(null);
      } else {
        console.error('Invalid response format:', response);
        console.error('Expected word data with "word" field, got:', wordData);
        setError('تنسيق الاستجابة غير صحيح - لا توجد كلمة في الاستجابة');
      }
      
    } catch (error) {
      console.error('Error generating word:', error);
      console.error('Error response:', error.response);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'خطأ في توليد الكلمة';
      
      setError(errorMessage);
      setGeneratedWord(null);
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateFamily = async () => {
    if (!selectedRoot) {
      alert('الرجاء إدخال الجذر');
      return;
    }

    setLoading(true);
    setError(null);
    
    console.log('Generating family for root:', selectedRoot.trim());
    
    try {
      const response = await api.generateFamily(selectedRoot.trim());
      
      console.log('Generate Family Response:', response);
      console.log('Response Data:', response.data);
      
      let familyData = null;
      
      // Vérifier si response.data a une structure ApiResponse (avec success)
      if (response.data && response.data.success === true) {
        familyData = response.data.data;
      }
      // Sinon, vérifier les autres formats possibles
      else if (Array.isArray(response.data)) {
        familyData = response.data;
      } else if (response.data?.family && Array.isArray(response.data.family)) {
        familyData = response.data.family;
      } else if (response.data?.data && Array.isArray(response.data.data)) {
        familyData = response.data.data;
      } else if (response.data?.result && Array.isArray(response.data.result)) {
        familyData = response.data.result;
      }
      
      console.log('Processed Family Data:', familyData);
      console.log('Family Data length:', familyData ? familyData.length : 0);
      
      if (familyData && Array.isArray(familyData) && familyData.length > 0) {
        setGeneratedFamily(familyData);
        setError(null);
      } else {
        console.error('Invalid response format or empty family:', response);
        setError('لا توجد عائلة صرفية لهذا الجذر');
        setGeneratedFamily([]);
      }
      
    } catch (error) {
      console.error('Error generating family:', error);
      console.error('Error response:', error.response);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'خطأ في توليد العائلة الصرفية';
      
      setError(errorMessage);
      setGeneratedFamily([]);
    } finally {
      setLoading(false);
    }
  };

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

  return (
    <div className="page-container">
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">›</span>
        <span>التوليد الصرفي</span>
      </div>

      <div className="page-header">
        <div className="ornament-icon">
          <Wand2 size={32} />
        </div>
        <h2 className="page-title">التوليد الصرفي</h2>
      </div>

      {/* Error Message */}
      {error && (
        <div className="result-box error" style={{ marginBottom: '20px' }}>
          <h4>✗ خطأ</h4>
          <p>{error}</p>
        </div>
      )}

      {/* Generate Single Word */}
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

          {generatedWord && generatedWord.word && (
            <div className="result-box success">
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

      {/* Generate Family */}
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
              className="btn btn-success btn-lg"
              onClick={handleGenerateFamily}
              disabled={loading}
            >
              {loading ? <Loader className="spin" size={20} /> : <FileText size={20} />}
              توليد العائلة الكاملة
            </button>
          </div>

          {generatedFamily.length > 0 && (
            <div className="result-box">
              <h4>العائلة الصرفية للجذر "{selectedRoot}"</h4>
              <div className="family-grid">
                {generatedFamily.map((item, index) => (
                  <div key={index} className="family-item">
                    <div className="family-word">{item.word || item}</div>
                    <div className="family-scheme">{item.scheme || ''}</div>
                  </div>
                ))}
              </div>
              <div className="family-stats">
                عدد الكلمات المولدة: {generatedFamily.length}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Generation;