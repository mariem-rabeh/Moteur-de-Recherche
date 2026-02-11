import React, { useState, useEffect } from 'react';
import { Upload, Search, Plus, Trash2, Edit, FileText } from 'lucide-react';
import api from '../services/api';

const SchemeManagement = () => {
  const [schemes, setSchemes] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newScheme, setNewScheme] = useState({ name: '', rule: '' });
  const [editMode, setEditMode] = useState(null);
  const [selectedFile, setSelectedFile] = useState(null);

  useEffect(() => {
    loadSchemes();
  }, []);

  const loadSchemes = async () => {
    try {
      const response = await api.getSchemes();
      setSchemes(response.data);
    } catch (error) {
      console.error('Error loading schemes:', error);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        await api.uploadSchemes(formData);
        loadSchemes();
        alert('تم تحميل الأوزان بنجاح');
      } catch (error) {
        alert('خطأ في تحميل الملف');
      }
    }
  };

  const handleAddScheme = async () => {
    if (newScheme.name.trim() && newScheme.rule.trim()) {
      try {
        await api.addScheme(newScheme.name, newScheme.rule);
        setNewScheme({ name: '', rule: '' });
        loadSchemes();
      } catch (error) {
        alert('خطأ في إضافة الوزن');
      }
    }
  };

  const handleUpdateScheme = async (name, rule) => {
    try {
      await api.updateScheme(name, rule);
      setEditMode(null);
      loadSchemes();
    } catch (error) {
      alert('خطأ في تعديل الوزن');
    }
  };

  const handleDeleteScheme = async (name) => {
    if (window.confirm(`هل تريد حذف الوزن "${name}"؟`)) {
      try {
        await api.deleteScheme(name);
        loadSchemes();
      } catch (error) {
        alert('خطأ في حذف الوزن');
      }
    }
  };

  const filteredSchemes = schemes.filter(scheme => 
    scheme.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="page-container">
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">›</span>
        <span>إدارة الأوزان الصرفية</span>
      </div>

      <div className="page-header">
        <div className="ornament-icon">
          <FileText size={32} />
        </div>
        <h2 className="page-title">إدارة الأوزان الصرفية</h2>
      </div>

      {/* Upload Section */}
      <div className="card">
        <div className="card-header">
          <FileText size={20} />
          <h3>تحميل الأوزان من ملف نصي</h3>
        </div>
        <div className="card-body">
          <div className="upload-section">
            <label htmlFor="file-upload-scheme" className="file-upload-label">
              <Upload size={20} />
              <span>اختر ملف</span>
              <input
                id="file-upload-scheme"
                type="file"
                accept=".txt"
                onChange={handleFileUpload}
                style={{ display: 'none' }}
              />
            </label>
            <span className="file-name">
              {selectedFile ? selectedFile.name : 'No file chosen'}
            </span>
            <button className="btn btn-primary" onClick={() => loadSchemes()}>
              تحميل
            </button>
          </div>
        </div>
      </div>

      {/* Add New Scheme */}
      <div className="card">
        <div className="card-header">
          <Plus size={20} />
          <h3>إضافة وزن جديد</h3>
        </div>
        <div className="card-body">
          <div className="action-row">
            <div className="search-box">
              <input
                type="text"
                placeholder="اسم الوزن (مثال: فاعل)"
                value={newScheme.name}
                onChange={(e) => setNewScheme({...newScheme, name: e.target.value})}
                className="search-input"
              />
            </div>
            <div className="search-box">
              <input
                type="text"
                placeholder="القاعدة (مثال: 1A2i3)"
                value={newScheme.rule}
                onChange={(e) => setNewScheme({...newScheme, rule: e.target.value})}
                className="search-input"
              />
            </div>
            <button className="btn btn-success" onClick={handleAddScheme}>
              إضافة
            </button>
          </div>
        </div>
      </div>

      {/* Search */}
      <div className="card">
        <div className="card-body">
          <div className="action-row">
            <div className="search-box">
              <Search size={20} className="search-icon" />
              <input
                type="text"
                placeholder="بحث عن وزن"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
              />
            </div>
            <button className="btn btn-secondary">
              <Search size={18} />
              بحث
            </button>
          </div>
        </div>
      </div>

      {/* Schemes List */}
      <div className="card">
        <div className="card-header">
          <div className="table-header">
            <span>الأوزان المتاحة ({filteredSchemes.length})</span>
            <span>عمليات</span>
          </div>
        </div>
        <div className="card-body">
          <div className="schemes-list">
            {filteredSchemes.map((scheme, index) => (
              <div key={index} className="scheme-item">
                {editMode === scheme.name ? (
                  <div className="scheme-edit-row">
                    <input
                      type="text"
                      defaultValue={scheme.rule}
                      className="edit-input"
                      id={`edit-rule-${index}`}
                    />
                    <button 
                      className="btn btn-success btn-sm"
                      onClick={() => {
                        const newRule = document.getElementById(`edit-rule-${index}`).value;
                        handleUpdateScheme(scheme.name, newRule);
                      }}
                    >
                      حفظ
                    </button>
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => setEditMode(null)}
                    >
                      إلغاء
                    </button>
                  </div>
                ) : (
                  <>
                    <div className="scheme-content">
                      <div className="scheme-name">{scheme.name}</div>
                      <div className="scheme-rule">القاعدة: {scheme.rule}</div>
                      <div className="scheme-example">
                        مثال: كتب → {scheme.result || 'كاتب'}
                      </div>
                    </div>
                    <div className="scheme-actions">
                      <button 
                        className="btn-icon btn-info"
                        onClick={() => setEditMode(scheme.name)}
                      >
                        <Edit size={18} />
                      </button>
                      <button 
                        className="btn-icon btn-danger"
                        onClick={() => handleDeleteScheme(scheme.name)}
                      >
                        <Trash2 size={18} />
                      </button>
                    </div>
                  </>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default SchemeManagement;