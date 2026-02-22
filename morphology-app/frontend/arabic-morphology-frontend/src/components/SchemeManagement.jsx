import React, { useState, useEffect } from 'react';
import { FileText, Upload, Plus, Edit2, Trash2, Save, X, Search } from 'lucide-react';
import api from '../services/api';

const SchemeManagement = () => {
  const [schemes, setSchemes] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newScheme, setNewScheme] = useState({ name: '', rule: '' });
  const [editMode, setEditMode] = useState(null);
  const [editRule, setEditRule] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    loadSchemes();
  }, []);

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

  const loadSchemes = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await api.getSchemes();
      console.log('API Response:', response.data); // Debug

      // Gérer différents formats de réponse
      let schemesData = [];
      
      if (response.data) {
        // Format 1: { success: true, data: [...] }
        if (response.data.data && Array.isArray(response.data.data)) {
          schemesData = response.data.data;
        }
        // Format 2: { success: true, data: { schemes: [...] } }
        else if (response.data.data && Array.isArray(response.data.data.schemes)) {
          schemesData = response.data.data.schemes;
        }
        // Format 3: Direct array
        else if (Array.isArray(response.data)) {
          schemesData = response.data;
        }
        // Format 4: { schemes: [...] }
        else if (response.data.schemes && Array.isArray(response.data.schemes)) {
          schemesData = response.data.schemes;
        }
      }

      console.log('Processed schemes:', schemesData); // Debug
      setSchemes(schemesData);

    } catch (err) {
      if (err.isExtensionError) return;
      console.error('Error loading schemes:', err);
      setError(err.message || 'فشل تحميل الأوزان الصرفية');
      setSchemes([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validation
    if (!file.name.endsWith('.txt')) {
      setError('يجب أن يكون الملف بصيغة .txt');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setError('حجم الملف كبير جداً (الحد الأقصى 5MB)');
      return;
    }

    setSelectedFile(file);
    setLoading(true);
    setError(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await api.uploadSchemes(formData);
      const data = response.data?.data || response.data;
      const count = typeof data === 'number' ? data : data?.count || 0;

      setSuccessMessage(`✓ تم تحميل ${count} وزن صرفي بنجاح`);
      setSelectedFile(null);
      loadSchemes();
      event.target.value = '';

    } catch (err) {
      if (err.isExtensionError) return;
      setError(err.message || 'فشل تحميل الملف');
      setSelectedFile(null);
    } finally {
      setLoading(false);
    }
  };

  const handleAddScheme = async () => {
    if (!newScheme.name.trim() || !newScheme.rule.trim()) {
      setError('الرجاء إدخال اسم الوزن والقاعدة');
      return;
    }

    // Validation: القاعدة يجب أن تحتوي على 1, 2, 3
    if (!newScheme.rule.includes('1') || !newScheme.rule.includes('2') || !newScheme.rule.includes('3')) {
      setError('القاعدة يجب أن تحتوي على الأرقام 1، 2، 3');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.addScheme(newScheme.name.trim(), newScheme.rule.trim());
      setSuccessMessage(`✓ تمت إضافة الوزن "${newScheme.name}" بنجاح`);
      setNewScheme({ name: '', rule: '' });
      loadSchemes();

    } catch (err) {
      if (err.isExtensionError) return;

      if (err.data?.message?.includes('existe déjà')) {
        setError('الوزن موجود بالفعل');
      } else {
        setError(err.message || 'فشلت إضافة الوزن');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateScheme = async (name) => {
    if (!editRule.trim()) {
      setError('الرجاء إدخال القاعدة');
      return;
    }

    if (!editRule.includes('1') || !editRule.includes('2') || !editRule.includes('3')) {
      setError('القاعدة يجب أن تحتوي على الأرقام 1، 2، 3');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.updateScheme(name, editRule.trim());
      setSuccessMessage(`✓ تم تحديث الوزن "${name}" بنجاح`);
      setEditMode(null);
      setEditRule('');
      loadSchemes();

    } catch (err) {
      if (err.isExtensionError) return;
      setError(err.message || 'فشل تحديث الوزن');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteScheme = async (name) => {
    if (!window.confirm(`هل تريد حذف الوزن "${name}"؟`)) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await api.deleteScheme(name);
      setSuccessMessage(`✓ تم حذف الوزن "${name}" بنجاح`);
      loadSchemes();

    } catch (err) {
      if (err.isExtensionError) return;
      setError(err.message || 'فشل حذف الوزن');
    } finally {
      setLoading(false);
    }
  };

  const startEdit = (scheme) => {
    setEditMode(scheme.name);
    setEditRule(scheme.rule);
  };

  const cancelEdit = () => {
    setEditMode(null);
    setEditRule('');
  };

  // Filtrage local
  const filteredSchemes = schemes.filter(scheme =>
    scheme && scheme.name && 
    scheme.name.toLowerCase().startsWith(searchTerm.toLowerCase())
  );

  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">←</span>
        <span className="active">إدارة الأوزان الصرفية</span>
      </div>

      {/* Page Header */}
      <div className="page-header">
        <FileText size={32} className="page-icon" />
        <div>
          <h1>إدارة الأوزان الصرفية</h1>
          <p>إضافة وتحرير وحذف الأوزان والقوالب المورفولوجية</p>
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
    <Upload size={20} />
    <h3>رفع ملف الأوزان</h3>
  </div>
  <div className="card-body">
    <input
      type="file"
      id="schemeFile"
      accept=".txt"
      onChange={handleFileUpload}
      style={{ display: 'none' }}
      disabled={loading}
    />
    <label
      htmlFor="schemeFile"
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
      {selectedFile ? selectedFile.name : 'انقر هنا لاختيار ملف .txt بتنسيق: اسم_الوزن|القاعدة'}
    </label>

    <p className="upload-hint" style={{ marginTop: 12 }}>
      • تنسيق الملف: فاعل|1ا2ِ3<br />
      • يجب أن يكون الملف بصيغة UTF-8
    </p>
  </div>
</div>      


      {/* Add New Scheme */}
      <div className="card">
        <div className="card-header">
          <Plus size={20} />
          <h3>إضافة وزن جديد</h3>
        </div>
        <div className="card-body">
          <div className="scheme-form">
            <div className="form-group">
              <label>اسم الوزن</label>
              <input
                type="text"
                className="form-input"
                placeholder="مثال: فاعل"
                value={newScheme.name}
                onChange={(e) => setNewScheme({ ...newScheme, name: e.target.value })}
                disabled={loading}
              />
            </div>
            <div className="form-group">
              <label>القاعدة (استخدم 1، 2، 3 لأحرف الجذر)</label>
              <input
                type="text"
                className="form-input"
                placeholder="مثال: 1ا2ِ3"
                value={newScheme.rule}
                onChange={(e) => setNewScheme({ ...newScheme, rule: e.target.value })}
                disabled={loading}
              />
            </div>
            <button
              className="btn btn-primary"
              onClick={handleAddScheme}
              disabled={loading || !newScheme.name.trim() || !newScheme.rule.trim()}
            >
              <Plus size={20} />
              إضافة وزن
            </button>
          </div>
          <div className="scheme-hint">
            <strong>نصيحة:</strong> استخدم الأرقام 1، 2، 3 لتمثيل أحرف الجذر الثلاثة.
            مثال: فاعل = 1ا2ِ3 (كتب → كاتِب)
          </div>
        </div>
      </div>

      {/* Search */}
      <div className="card">
        <div className="card-body">
          <div className="search-box" style={{
  display: 'flex',
  alignItems: 'center',
  gap: '10px',
  border: '2px solid #ddd',
  borderRadius: '8px',
  padding: '8px 14px',
  background: 'white',
  transition: 'all 0.3s',
}}>
  <Search size={20} style={{ color: '#999', flexShrink: 0 }} />
  <input
    type="text"
    placeholder="ابحث عن وزن..."
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
        </div>
      </div>

      {/* Schemes List */}
      <div className="card">
        <div className="card-header">
          <FileText size={20} />
          <h3>قائمة الأوزان الصرفية</h3>
          <span className="root-count">{filteredSchemes.length}</span>
        </div>
        <div className="card-body">
          {loading && schemes.length === 0 ? (
            <div className="loading-state">جاري التحميل...</div>
          ) : filteredSchemes.length === 0 ? (
            <div className="empty-state">
              <FileText size={48} />
              <p>لا توجد أوزان صرفية بعد</p>
              <p className="empty-hint">ابدأ بإضافة وزن أو رفع ملف</p>
            </div>
          ) : (
            <div className="schemes-list">
              {filteredSchemes.map((scheme, index) => (
                <div key={index} className="scheme-item">
                  <div className="scheme-info">
                    <div className="scheme-name">{scheme.name}</div>
                    {editMode === scheme.name ? (
                      <input
                        type="text"
                        className="form-input edit-input"
                        value={editRule}
                        onChange={(e) => setEditRule(e.target.value)}
                        disabled={loading}
                      />
                    ) : (
                      <div className="scheme-rule">{scheme.rule}</div>
                    )}
                    <div className="scheme-example">
                      مثال: كتب → {scheme.rule.replace('1', 'ك').replace('2', 'ت').replace('3', 'ب')}
                    </div>
                  </div>
                  <div className="scheme-actions">
                    {editMode === scheme.name ? (
                      <>
                        <button
                          className="btn btn-icon btn-success"
                          onClick={() => handleUpdateScheme(scheme.name)}
                          title="حفظ"
                          disabled={loading}
                        >
                          <Save size={16} />
                        </button>
                        <button
                          className="btn btn-icon btn-secondary"
                          onClick={cancelEdit}
                          title="إلغاء"
                          disabled={loading}
                        >
                          <X size={16} />
                        </button>
                      </>
                    ) : (
                      <>
                        <button
                          className="btn btn-icon btn-primary"
                          onClick={() => startEdit(scheme)}
                          title="تعديل"
                          disabled={loading}
                        >
                          <Edit2 size={16} />
                        </button>
                        <button
                          className="btn btn-icon btn-danger"
                          onClick={() => handleDeleteScheme(scheme.name)}
                          title="حذف"
                          disabled={loading}
                        >
                          <Trash2 size={16} />
                        </button>
                      </>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SchemeManagement;