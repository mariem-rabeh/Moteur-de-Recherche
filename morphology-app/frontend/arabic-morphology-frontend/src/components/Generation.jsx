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

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [rootsRes, schemesRes] = await Promise.all([
        api.getRoots('', 1, 1000),
        api.getSchemes()
      ]);
      setRoots(rootsRes.data.roots);
      setSchemes(schemesRes.data);
    } catch (error) {
      console.error('Error loading data:', error);
    }
  };

  const handleGenerateWord = async () => {
    if (!selectedRoot || !selectedScheme) {
      alert('الرجاء اختيار جذر ووزن');
      return;
    }

    setLoading(true);
    try {
      const response = await api.generateWord(selectedRoot, selectedScheme);
      setGeneratedWord(response.data);
    } catch (error) {
      alert('خطأ في توليد الكلمة');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateFamily = async () => {
    if (!selectedRoot) {
      alert('الرجاء اختيار جذر');
      return;
    }

    setLoading(true);
    try {
      const response = await api.generateFamily(selectedRoot);
      setGeneratedFamily(response.data);
    } catch (error) {
      alert('خطأ في توليد العائلة الصرفية');
    } finally {
      setLoading(false);
    }
  };

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
              <select 
                value={selectedRoot}
                onChange={(e) => setSelectedRoot(e.target.value)}
                className="form-select"
              >
                <option value="">-- اختر جذراً --</option>
                {roots.map((root, index) => (
                  <option key={index} value={root}>{root}</option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>اختر الوزن:</label>
              <select 
                value={selectedScheme}
                onChange={(e) => setSelectedScheme(e.target.value)}
                className="form-select"
              >
                <option value="">-- اختر وزناً --</option>
                {schemes.map((scheme, index) => (
                  <option key={index} value={scheme.name}>
                    {scheme.name} - {scheme.rule}
                  </option>
                ))}
              </select>
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

          {generatedWord && (
            <div className="result-box success">
              <h4>✓ النتيجة</h4>
              <div className="generated-word">
                <div className="word-display">{generatedWord.word}</div>
                <div className="word-details">
                  <span>الجذر: {generatedWord.root}</span>
                  <span>الوزن: {generatedWord.scheme}</span>
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
              <select 
                value={selectedRoot}
                onChange={(e) => setSelectedRoot(e.target.value)}
                className="form-select"
              >
                <option value="">-- اختر جذراً --</option>
                {roots.map((root, index) => (
                  <option key={index} value={root}>{root}</option>
                ))}
              </select>
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
                    <div className="family-word">{item.word}</div>
                    <div className="family-scheme">{item.scheme}</div>
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