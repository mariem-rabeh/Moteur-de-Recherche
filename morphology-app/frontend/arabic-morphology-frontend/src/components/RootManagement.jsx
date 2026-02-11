import React, { useState, useEffect } from 'react';
import { Upload, Search, Plus, Trash2, Edit, FileText } from 'lucide-react';
import api from '../services/api';
import { BookOpen } from 'lucide-react';

const RootManagement = () => {
  const [roots, setRoots] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [newRoot, setNewRoot] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [selectedFile, setSelectedFile] = useState(null);
  const itemsPerPage = 10;

  useEffect(() => {
    loadRoots();
  }, [currentPage, searchTerm]);

  const loadRoots = async () => {
  try {
    const response = await api.getRoots(searchTerm, currentPage, itemsPerPage);

    const rootsData = response.data?.roots || response.data || [];

    setRoots(Array.isArray(rootsData) ? rootsData : []);
    setTotalPages(response.data?.totalPages || 1);

  } catch (error) {
    console.error('Error loading roots:', error);
    setRoots([]); // sécurité
  }
};


  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedFile(file);
      const formData = new FormData();
      formData.append('file', file);
      
      try {
        await api.uploadRoots(formData);
        loadRoots();
        alert('تم تحميل الجذور بنجاح');
      } catch (error) {
        alert('خطأ في تحميل الملف');
      }
    }
  };

  const handleAddRoot = async () => {
    if (newRoot.trim()) {
      try {
        await api.addRoot(newRoot);
        setNewRoot('');
        loadRoots();
      } catch (error) {
        alert('خطأ في إضافة الجذر');
      }
    }
  };

  const handleDeleteRoot = async (root) => {
    if (window.confirm(`هل تريد حذف الجذر "${root}"؟`)) {
      try {
        await api.deleteRoot(root);
        loadRoots();
      } catch (error) {
        alert('خطأ في حذف الجذر');
      }
    }
  };

  const filteredRoots = (roots || []).filter(root =>
  root?.toLowerCase().includes(searchTerm.toLowerCase())
);


  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">›</span>
        <span>إدارة الجذور</span>
      </div>

      {/* Page Title */}
      <div className="page-header">
        <div className="ornament-icon">
          <BookOpen size={32} />
        </div>
        <h2 className="page-title">إدارة الجذور</h2>
      </div>

      {/* Upload Section */}
      <div className="card">
        <div className="card-header">
          <FileText size={20} />
          <h3>تحميل الجذور من ملف نصي</h3>
        </div>
        <div className="card-body">
          <div className="upload-section">
            <label htmlFor="file-upload" className="file-upload-label">
              <Upload size={20} />
              <span>اختر ملف</span>
              <input
                id="file-upload"
                type="file"
                accept=".txt"
                onChange={handleFileUpload}
                style={{ display: 'none' }}
              />
            </label>
            <span className="file-name">
              {selectedFile ? selectedFile.name : 'No file chosen'}
            </span>
            <button className="btn btn-primary" onClick={() => loadRoots()}>
              تحميل
            </button>
          </div>
        </div>
      </div>

      {/* Search and Add Section */}
      <div className="card">
        <div className="card-body">
          <div className="action-row">
            {/* Search */}
            <div className="search-box">
              <Search size={20} className="search-icon" />
              <input
                type="text"
                placeholder="إدخال جذر"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
              />
            </div>
            <button className="btn btn-secondary">
              <Search size={18} />
              بحث
            </button>

            {/* Add Root */}
            <div className="search-box">
              <Plus size={20} className="search-icon" />
              <input
                type="text"
                placeholder="إدخال جذر"
                value={newRoot}
                onChange={(e) => setNewRoot(e.target.value)}
                className="search-input"
              />
            </div>
            <button className="btn btn-success" onClick={handleAddRoot}>
              إضافة
            </button>
          </div>
        </div>
      </div>

      {/* Roots Table */}
      <div className="card">
        <div className="card-header">
          <div className="table-header">
            <span>عرض الجذور</span>
            <span>عمليات</span>
          </div>
        </div>
        <div className="card-body">
          <button className="btn btn-danger mb-3">
            حذف
          </button>

          <div className="roots-list">
            {filteredRoots.map((root, index) => (
              <div key={index} className="root-item">
                <div className="root-content">
                  <span className="root-text">{root}</span>
                  <span className="root-count">8</span>
                </div>
                <div className="root-actions">
                  <button className="btn-icon btn-info">
                    <FileText size={18} />
                  </button>
                  <button 
                    className="btn-icon btn-danger"
                    onClick={() => handleDeleteRoot(root)}
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          <div className="pagination">
            <button 
              className="pagination-btn"
              onClick={() => setCurrentPage(1)}
              disabled={currentPage === 1}
            >
              «
            </button>
            <button 
              className="pagination-btn"
              onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
              disabled={currentPage === 1}
            >
              ‹
            </button>
            <span className="pagination-info">
              {currentPage} من 4
            </span>
            <button 
              className="pagination-btn"
              onClick={() => setCurrentPage(Math.min(totalPages, currentPage + 1))}
              disabled={currentPage === totalPages}
            >
              ›
            </button>
            <button 
              className="pagination-btn"
              onClick={() => setCurrentPage(totalPages)}
              disabled={currentPage === totalPages}
            >
              »
            </button>
          </div>
        </div>
      </div>

      {/* Success Message */}
      <div className="success-message">
        ✓ تحميل 15 جذراً بنجاح من الملف
      </div>
    </div>
  );
};

export default RootManagement;