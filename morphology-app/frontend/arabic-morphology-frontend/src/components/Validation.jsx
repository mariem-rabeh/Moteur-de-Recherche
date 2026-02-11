import React, { useState } from 'react';
import { CheckCircle, Search, AlertCircle } from 'lucide-react';
import api from '../services/api';

const Validation = () => {
  const [word, setWord] = useState('');
  const [root, setRoot] = useState('');
  const [validationResult, setValidationResult] = useState(null);
  const [decompositionResult, setDecompositionResult] = useState(null);
  const [allRootsResult, setAllRootsResult] = useState([]);

  const handleValidate = async () => {
    if (!word || !root) {
      alert('الرجاء إدخال الكلمة والجذر');
      return;
    }

    try {
      const response = await api.validateWord(word, root);
      setValidationResult(response.data);
    } catch (error) {
      alert('خطأ في التحقق');
    }
  };

  const handleDecompose = async () => {
    if (!word) {
      alert('الرجاء إدخال كلمة');
      return;
    }

    try {
      const response = await api.decomposeWord(word);
      setDecompositionResult(response.data);
    } catch (error) {
      alert('خطأ في التحليل');
    }
  };

  const handleFindAllRoots = async () => {
    if (!word) {
      alert('الرجاء إدخال كلمة');
      return;
    }

    try {
      const response = await api.findAllRoots(word);
      setAllRootsResult(response.data);
    } catch (error) {
      alert('خطأ في البحث');
    }
  };

  return (
    <div className="page-container">
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">›</span>
        <span>الصحة الصرفية</span>
      </div>

      <div className="page-header">
        <div className="ornament-icon">
          <CheckCircle size={32} />
        </div>
        <h2 className="page-title">التحقق من الصحة الصرفية</h2>
      </div>

      {/* Validate Word */}
      <div className="card">
        <div className="card-header">
          <CheckCircle size={20} />
          <h3>التحقق من انتماء كلمة لجذر</h3>
        </div>
        <div className="card-body">
          <div className="validation-form">
            <div className="form-row">
              <div className="form-group">
                <label>الكلمة:</label>
                <input
                  type="text"
                  value={word}
                  onChange={(e) => setWord(e.target.value)}
                  className="form-input"
                  placeholder="أدخل الكلمة"
                />
              </div>
              <div className="form-group">
                <label>الجذر المفترض:</label>
                <input
                  type="text"
                  value={root}
                  onChange={(e) => setRoot(e.target.value)}
                  className="form-input"
                  placeholder="أدخل الجذر"
                />
              </div>
            </div>
            <button className="btn btn-primary" onClick={handleValidate}>
              <CheckCircle size={18} />
              التحقق
            </button>
          </div>

          {validationResult && (
            <div className={`result-box ${validationResult.valid ? 'success' : 'error'}`}>
              {validationResult.valid ? (
                <>
                  <h4>✓ التحقق ناجح</h4>
                  <p>الكلمة "{word}" تنتمي للجذر "{root}"</p>
                  <p>الوزن المستخدم: {validationResult.scheme}</p>
                </>
              ) : (
                <>
                  <h4>✗ التحقق فشل</h4>
                  <p>{validationResult.message}</p>
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Decompose Word */}
      <div className="card">
        <div className="card-header">
          <Search size={20} />
          <h3>تحليل كلمة إلى جذر ووزن</h3>
        </div>
        <div className="card-body">
          <div className="validation-form">
            <div className="form-group">
              <label>الكلمة:</label>
              <input
                type="text"
                value={word}
                onChange={(e) => setWord(e.target.value)}
                className="form-input"
                placeholder="أدخل الكلمة للتحليل"
              />
            </div>
            <button className="btn btn-secondary" onClick={handleDecompose}>
              <Search size={18} />
              تحليل
            </button>
          </div>

          {decompositionResult && (
            <div className="result-box">
              <h4>نتيجة التحليل</h4>
              <div className="decomposition-details">
                <div className="detail-item">
                  <span className="label">الكلمة:</span>
                  <span className="value">{decompositionResult.word}</span>
                </div>
                <div className="detail-item">
                  <span className="label">الجذر:</span>
                  <span className="value">{decompositionResult.root}</span>
                </div>
                <div className="detail-item">
                  <span className="label">الوزن:</span>
                  <span className="value">{decompositionResult.scheme}</span>
                </div>
                {decompositionResult.additions && (
                  <div className="detail-item">
                    <span className="label">العناصر المضافة:</span>
                    <span className="value">{decompositionResult.additions.join(', ')}</span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Find All Possible Roots */}
      <div className="card">
        <div className="card-header">
          <AlertCircle size={20} />
          <h3>إيجاد جميع الجذور الممكنة</h3>
        </div>
        <div className="card-body">
          <div className="validation-form">
            <div className="form-group">
              <label>الكلمة:</label>
              <input
                type="text"
                value={word}
                onChange={(e) => setWord(e.target.value)}
                className="form-input"
                placeholder="أدخل الكلمة"
              />
            </div>
            <button className="btn btn-success" onClick={handleFindAllRoots}>
              <Search size={18} />
              بحث شامل
            </button>
          </div>

          {allRootsResult.length > 0 && (
            <div className="result-box">
              <h4>الجذور الممكنة ({allRootsResult.length})</h4>
              <div className="possible-roots">
                {allRootsResult.map((item, index) => (
                  <div key={index} className="possible-root-item">
                    <div className="root-number">{index + 1}</div>
                    <div className="root-info">
                      <span className="root-text">الجذر: {item.root}</span>
                      <span className="scheme-text">الوزن: {item.scheme}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Validation;