import React, { useState, useEffect } from 'react';
import { BookOpen, Upload, Plus, Trash2, Search } from 'lucide-react';
import api from '../services/api';

const RootManagement = () => {
  const [roots, setRoots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newRoot, setNewRoot] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  
  const itemsPerPage = 10;

  useEffect(() => {
    loadRoots();
  }, [currentPage, searchTerm]);

  // Auto-clear messages après 5 secondes
  useEffect(() => {
    if (successMessage || error) {
      const timer = setTimeout(() => {
        setSuccessMessage(null);
        setError(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [successMessage, error]);

  const loadRoots = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await api.getRoots(searchTerm, currentPage, itemsPerPage);
      
      // Gestion flexible du format de réponse
      const data = response.data?.data || response.data;
      const rootsData = data?.roots || data || [];
      
      setRoots(Array.isArray(rootsData) ? rootsData : []);
      setTotalPages(data?.totalPages || Math.ceil((data?.total || rootsData.length) / itemsPerPage));
      
    } catch (err) {
      // Ignorer les erreurs d'extension
      if (err.isExtensionError) {
        console.warn('Extension error ignored');
        return;
      }
      
      setError(err.message || 'فشل تحميل الجذور');
      setRoots([]);
      
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validation du fichier
    if (!file.name.endsWith('.txt')) {
      setError('يجب أن يكون الملف بصيغة .txt');
      return;
    }

    if (file.size > 5 * 1024 * 1024) { // 5MB max
      setError('حجم الملف كبير جداً (الحد الأقصى 5MB)');
      return;
    }

    setSelectedFile(file);
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.uploadRoots(formData);
      const data = response.data?.data || response.data;
      const count = typeof data === 'number' ? data : data?.count || 0;
      
      setSuccessMessage(`✓ تم تحميل ${count} جذرًا بنجاح من الملف`);
      setSelectedFile(null);
      loadRoots();
      
      // Reset file input
      event.target.value = '';
      
    } catch (err) {
      if (err.isExtensionError) return;
      setError(err.message || 'فشل تحميل الملف');
      setSelectedFile(null);
      
    } finally {
      setLoading(false);
    }
  };

  const handleAddRoot = async () => {
    if (!newRoot.trim()) {
      setError('الرجاء إدخال جذر');
      return;
    }

    // Validation: doit contenir 3 caractères arabes
    if (newRoot.trim().length !== 3) {
      setError('الجذر يجب أن يحتوي على 3 أحرف عربية');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.addRoot(newRoot.trim());
      setSuccessMessage(`✓ تمت إضافة الجذر "${newRoot}" بنجاح`);
      setNewRoot('');
      loadRoots();
      
    } catch (err) {
      if (err.isExtensionError) return;
      
      // Gérer le cas où la racine existe déjà
      if (err.data?.message?.includes('existe déjà')) {
        setError('الجذر موجود بالفعل');
      } else {
        setError(err.message || 'فشلت إضافة الجذر');
      }
      
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteRoot = async (root) => {
    if (!window.confirm(`هل تريد حذف الجذر "${root}"؟`)) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.deleteRoot(root);
      setSuccessMessage(`✓ تم حذف الجذر "${root}" بنجاح`);
      loadRoots();
      
    } catch (err) {
      if (err.isExtensionError) return;
      setError(err.message || 'فشل حذف الجذر');
      
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setCurrentPage(newPage);
    }
  };

  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">←</span>
        <span className="active">إدارة الجذور</span>
      </div>

      {/* Page Header */}
      <div className="page-header">
        <BookOpen size={32} className="page-icon" />
        <div>
          <h1>إدارة الجذور</h1>
          <p>إضافة وتحرير وحذف الجذور الثلاثية العربية</p>
        </div>
      </div>

      {/* Messages */}
      {error && (
        <div className="alert alert-danger">
          <strong>خطأ:</strong> {error}
        </div>
      )}

      {successMessage && (
        <div className="alert alert-success">
          {successMessage}
        </div>
      )}

      {/* Upload Section */}
    <div className="card upload-card">
      <div className="card-header">
        <Upload size={24} />
        <h3>رفع ملف الجذور</h3>
      </div>
      <div className="card-body">
        <input
          type="file"
          id="rootFile"
          accept=".txt"
          onChange={handleFileUpload}
          style={{ display: 'none' }}
          disabled={loading}
        />
        <label
          htmlFor="rootFile"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '16px 20px',
            border: '2px dashed #b8935f',
            borderRadius: '8px',
            background: '#fdfaf7',
            cursor: loading ? 'not-allowed' : 'pointer',
            fontFamily: 'var(--font-arabic)',
            color: selectedFile ? '#2d6a6b' : '#888',
            fontWeight: selectedFile ? 600 : 400,
            transition: 'all 0.2s',
          }}
        >
          {selectedFile ? selectedFile.name : 'انقر هنا لاختيار ملف .txt'}
        </label>

        <p className="upload-hint" style={{ marginTop: 12 }}>
          • الملف يجب أن يكون بصيغة UTF-8<br />
          • جذر واحد في كل سطر<br />
          • كل جذر يجب أن يحتوي على 3 أحرف عربية
        </p>
      </div>
    </div>

      {/* Search and Add */}
<div className="card">
  <div className="card-body">
    <div className="action-row">

      {/* Search box corrigé */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
        border: '2px solid #ddd',
        borderRadius: '8px',
        padding: '8px 14px',
        background: 'white',
        flex: 1,
      }}>
        <Search size={20} style={{ color: '#999', flexShrink: 0 }} />
        <input
          type="text"
          placeholder="ابحث عن جذر..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          disabled={loading}
          style={{
            border: 'none',
            outline: 'none',
            width: '100%',
            fontFamily: 'var(--font-arabic)',
            fontSize: '0.95rem',
            background: 'transparent',
            direction: 'rtl',
          }}
        />
      </div>

      <div className="add-root-section" style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
  <input
    type="text"
    placeholder="جذر جديد (مثال: كتب)"
    value={newRoot}
    onChange={(e) => setNewRoot(e.target.value)}
    onKeyPress={(e) => e.key === 'Enter' && handleAddRoot()}
    maxLength={3}
    disabled={loading}
    style={{
      padding: '10px 16px',
      border: '2px solid #ddd',
      borderRadius: '8px',
      fontFamily: 'var(--font-arabic)',
      fontSize: '1rem',
      outline: 'none',
      direction: 'rtl',
      width: '180px',
      transition: 'border-color 0.2s',
    }}
  />
  <button
        onClick={handleAddRoot}
        disabled={loading || !newRoot.trim()}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '10px 20px',
          background: loading || !newRoot.trim()
            ? '#ccc'
            : 'linear-gradient(135deg, #2d6a6b 0%, #0d3b3f 100%)',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          fontFamily: 'var(--font-arabic)',
          fontSize: '0.95rem',
          fontWeight: 700,
          cursor: loading || !newRoot.trim() ? 'not-allowed' : 'pointer',
          boxShadow: loading || !newRoot.trim() ? 'none' : '0 3px 10px rgba(13,59,63,0.3)',
          transition: 'all 0.2s',
          whiteSpace: 'nowrap',
        }}
      >
        <Plus size={18} />
        إضافة جذر
      </button>
    </div>

    </div>
  </div>
</div>
      {/* Roots List */}
      <div className="card">
        <div className="card-header">
          <BookOpen size={20} />
          <h3>قائمة الجذور</h3>
          <span className="root-count">{roots.length}</span>
        </div>
        <div className="card-body">
          {loading && roots.length === 0 ? (
            <div className="loading-state">جاري التحميل...</div>
          ) : roots.length === 0 ? (
            <div className="empty-state">
              <BookOpen size={48} />
              <p>لا توجد جذور بعد</p>
              <p className="empty-hint">ابدأ بإضافة جذر أو رفع ملف</p>
            </div>
          ) : (
            <>
              <div className="roots-grid">
                {roots.map((root, index) => (
                  <div key={index} className="root-item">
                    <span className="root-text">{root}</span>
                    <button
                      className="btn btn-icon btn-danger"
                      onClick={() => handleDeleteRoot(root)}
                      title="حذف"
                      disabled={loading}
                    >
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="pagination">
                  <button
                    className="pagination-btn"
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1 || loading}
                  >
                    السابق
                  </button>
                  
                  <span className="pagination-info">
                    {currentPage} من {totalPages}
                  </span>
                  
                  <button
                    className="pagination-btn"
                    onClick={() => handlePageChange(currentPage + 1)}
                    disabled={currentPage === totalPages || loading}
                  >
                    التالي
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default RootManagement;