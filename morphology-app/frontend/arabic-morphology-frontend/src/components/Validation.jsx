import React, { useState } from 'react';
import { CheckCircle, Search, AlertCircle, Loader } from 'lucide-react';
import api from '../services/api';

const Validation = () => {
  // États séparés pour chaque section (meilleure pratique)
  const [validateWord, setValidateWord] = useState('');
  const [validateRoot, setValidateRoot] = useState('');
  const [decomposeWord, setDecomposeWord] = useState('');
  const [findRootsWord, setFindRootsWord] = useState('');
  
  const [validationResult, setValidationResult] = useState(null);
  const [decompositionResult, setDecompositionResult] = useState(null);
  const [allRootsResult, setAllRootsResult] = useState([]);
  
  const [loadingValidate, setLoadingValidate] = useState(false);
  const [loadingDecompose, setLoadingDecompose] = useState(false);
  const [loadingFindRoots, setLoadingFindRoots] = useState(false);
  
  const [error, setError] = useState(null);

  const handleValidate = async () => {
    if (!validateWord.trim() || !validateRoot.trim()) {
      alert('الرجاء إدخال الكلمة والجذر');
      return;
    }

    setLoadingValidate(true);
    setError(null);
    
    console.log('Validating:', { 
      word: validateWord.trim(), 
      root: validateRoot.trim() 
    });

    try {
      const response = await api.validateWord(
        validateWord.trim(), 
        validateRoot.trim()
      );
      
      console.log('Validation Response:', response);
      console.log('Validation Data:', response.data);
      
      // Gestion de différents formats
      let resultData = null;
      
      // Vérifier si response.data a une structure ApiResponse (avec success)
      if (response.data && response.data.success === true) {
        resultData = response.data.data;
      }
      // Sinon, vérifier les autres formats possibles
      else if (response.data) {
        if (response.data.valid !== undefined) {
          resultData = response.data;
        } else if (response.data.result) {
          resultData = response.data.result;
        } else if (response.data.data) {
          resultData = response.data.data;
        }
      }
      
      console.log('Processed Validation Data:', resultData);
      console.log('Validation Data keys:', resultData ? Object.keys(resultData) : 'null');
      
      if (resultData && resultData.valid !== undefined) {
        setValidationResult(resultData);
        setError(null);
      } else {
        console.error('Invalid validation response format:', response);
        setError('تنسيق الاستجابة غير صحيح');
      }
      
    } catch (error) {
      console.error('Error validating:', error);
      console.error('Error response:', error.response);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'خطأ في التحقق';
      
      setError(errorMessage);
      setValidationResult(null);
    } finally {
      setLoadingValidate(false);
    }
  };

  const handleDecompose = async () => {
    if (!decomposeWord.trim()) {
      alert('الرجاء إدخال كلمة');
      return;
    }

    setLoadingDecompose(true);
    setError(null);
    
    console.log('Decomposing word:', decomposeWord.trim());

    try {
      const response = await api.decomposeWord(decomposeWord.trim());
      
      console.log('Decompose Response:', response);
      console.log('Decompose Data:', response.data);
      
      // Gestion de différents formats
      let resultData = null;
      
      // Vérifier si response.data a une structure ApiResponse (avec success)
      if (response.data && response.data.success === true) {
        resultData = response.data.data;
      }
      // Sinon, vérifier les autres formats possibles
      else if (response.data) {
        if (response.data.word || response.data.root) {
          resultData = response.data;
        } else if (response.data.result) {
          resultData = response.data.result;
        } else if (response.data.data) {
          resultData = response.data.data;
        }
      }
      
      console.log('Processed Decompose Data:', resultData);
      console.log('Decompose Data keys:', resultData ? Object.keys(resultData) : 'null');
      
      if (resultData && (resultData.word || resultData.root)) {
        setDecompositionResult(resultData);
        setError(null);
      } else {
        console.error('Invalid decompose response format:', response);
        setError('تنسيق الاستجابة غير صحيح');
      }
      
    } catch (error) {
      console.error('Error decomposing:', error);
      console.error('Error response:', error.response);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'خطأ في التحليل';
      
      setError(errorMessage);
      setDecompositionResult(null);
    } finally {
      setLoadingDecompose(false);
    }
  };

  const handleFindAllRoots = async () => {
    if (!findRootsWord.trim()) {
      alert('الرجاء إدخال كلمة');
      return;
    }

    setLoadingFindRoots(true);
    setError(null);
    
    console.log('Finding all roots for:', findRootsWord.trim());

    try {
      const response = await api.findAllRoots(findRootsWord.trim());
      
      console.log('Find Roots Response:', response);
      console.log('Find Roots Data:', response.data);
      
      // Gestion de différents formats
      let resultData = null;
      
      // Vérifier si response.data a une structure ApiResponse (avec success)
      if (response.data && response.data.success === true) {
        resultData = response.data.data;
      }
      // Sinon, vérifier les autres formats possibles
      else if (Array.isArray(response.data)) {
        resultData = response.data;
      } else if (response.data?.roots && Array.isArray(response.data.roots)) {
        resultData = response.data.roots;
      } else if (response.data?.data && Array.isArray(response.data.data)) {
        resultData = response.data.data;
      } else if (response.data?.result && Array.isArray(response.data.result)) {
        resultData = response.data.result;
      }
      
      console.log('Processed Find Roots Data:', resultData);
      console.log('Find Roots Data length:', resultData ? resultData.length : 0);
      
      if (resultData && Array.isArray(resultData) && resultData.length > 0) {
        setAllRootsResult(resultData);
        setError(null);
      } else {
        console.error('Invalid find roots response format or no roots found:', response);
        setAllRootsResult([]);
        setError('لم يتم العثور على جذور');
      }
      
    } catch (error) {
      console.error('Error finding roots:', error);
      console.error('Error response:', error.response);
      
      const errorMessage = error.response?.data?.message || 
                          error.message || 
                          'خطأ في البحث';
      
      setError(errorMessage);
      setAllRootsResult([]);
    } finally {
      setLoadingFindRoots(false);
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

      {/* Error Message */}
      {error && (
        <div className="result-box error" style={{ marginBottom: '20px' }}>
          <h4>✗ خطأ</h4>
          <p>{error}</p>
        </div>
      )}

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
                  value={validateWord}
                  onChange={(e) => setValidateWord(e.target.value.trim())}
                  className="form-input"
                  placeholder="أدخل الكلمة (مثال: كاتب)"
                />
              </div>
              <div className="form-group">
                <label>الجذر المفترض:</label>
                <input
                  type="text"
                  value={validateRoot}
                  onChange={(e) => setValidateRoot(e.target.value.trim())}
                  className="form-input"
                  placeholder="أدخل الجذر (مثال: كتب)"
                />
              </div>
            </div>
            <button 
              className="btn btn-primary" 
              onClick={handleValidate}
              disabled={loadingValidate}
            >
              {loadingValidate ? <Loader className="spin" size={18} /> : <CheckCircle size={18} />}
              التحقق
            </button>
          </div>

          {validationResult && validationResult.valid !== undefined && (
            <div className={`result-box ${validationResult.valid ? 'success' : 'error'}`}>
              {validationResult.valid ? (
                <>
                  <h4>✓ التحقق ناجح</h4>
                  <p>الكلمة "{validateWord}" تنتمي للجذر "{validateRoot}"</p>
                  {validationResult.scheme && (
                    <p>الوزن المستخدم: {validationResult.scheme}</p>
                  )}
                </>
              ) : (
                <>
                  <h4>✗ التحقق فشل</h4>
                  <p>{validationResult.message || 'الكلمة لا تنتمي للجذر المعطى'}</p>
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
                value={decomposeWord}
                onChange={(e) => setDecomposeWord(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الكلمة للتحليل (مثال: كاتب)"
              />
            </div>
            <button 
              className="btn btn-secondary" 
              onClick={handleDecompose}
              disabled={loadingDecompose}
            >
              {loadingDecompose ? <Loader className="spin" size={18} /> : <Search size={18} />}
              تحليل
            </button>
          </div>

          {decompositionResult && (
            <div className="result-box">
              <h4>نتيجة التحليل</h4>
              <div className="decomposition-details">
                {decompositionResult.word && (
                  <div className="detail-item">
                    <span className="label">الكلمة:</span>
                    <span className="value">{decompositionResult.word}</span>
                  </div>
                )}
                {decompositionResult.root && (
                  <div className="detail-item">
                    <span className="label">الجذر:</span>
                    <span className="value">{decompositionResult.root}</span>
                  </div>
                )}
                {decompositionResult.scheme && (
                  <div className="detail-item">
                    <span className="label">الوزن:</span>
                    <span className="value">{decompositionResult.scheme}</span>
                  </div>
                )}
                {decompositionResult.additions && decompositionResult.additions.length > 0 && (
                  <div className="detail-item">
                    <span className="label">العناصر المضافة:</span>
                    <span className="value">
                      {Array.isArray(decompositionResult.additions) 
                        ? decompositionResult.additions.join(', ')
                        : decompositionResult.additions}
                    </span>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Find All Roots */}
      <div className="card">
        <div className="card-header">
          <Search size={20} />
          <h3>إيجاد جميع الجذور المحتملة</h3>
        </div>
        <div className="card-body">
          <div className="validation-form">
            <div className="form-group">
              <label>الكلمة:</label>
              <input
                type="text"
                value={findRootsWord}
                onChange={(e) => setFindRootsWord(e.target.value.trim())}
                className="form-input"
                placeholder="أدخل الكلمة للبحث (مثال: مكتوب)"
              />
            </div>
            <button 
              className="btn btn-info" 
              onClick={handleFindAllRoots}
              disabled={loadingFindRoots}
            >
              {loadingFindRoots ? <Loader className="spin" size={18} /> : <Search size={18} />}
              بحث عن الجذور
            </button>
          </div>

          {allRootsResult.length > 0 && (
            <div className="result-box">
              <h4>الجذور المحتملة للكلمة "{findRootsWord}"</h4>
              <div className="roots-list">
                {allRootsResult.map((item, index) => (
                  <div key={index} className="root-result-item">
                    {typeof item === 'string' ? (
                      <span className="root-value">{item}</span>
                    ) : (
                      <>
                        <span className="root-value">{item.root || item}</span>
                        {item.scheme && (
                          <span className="scheme-badge">{item.scheme}</span>
                        )}
                      </>
                    )}
                  </div>
                ))}
              </div>
              <div className="roots-count">
                عدد الجذور المحتملة: {allRootsResult.length}
              </div>
            </div>
          )}
        </div>
      </div>

    </div>
  );
};

export default Validation;