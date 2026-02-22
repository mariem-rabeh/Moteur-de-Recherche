import React, { useState, useEffect } from 'react';
import { BarChart3, TrendingUp, Database } from 'lucide-react';
import api from '../services/api';

const Statistics = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await api.getStatistics();
      console.log('Full API Response:', response);
      console.log('Response.data:', response.data);
      
      // Gérer différents formats de réponse
      let statsData = null;
      
      if (response.data) {
        // Format 1: { success: true, data: {...} }
        if (response.data.data) {
          statsData = response.data.data;
        }
        // Format 2: Direct data
        else if (response.data.totalRoots !== undefined) {
          statsData = response.data;
        }
      }
      
      console.log('Processed stats:', statsData);
      setStats(statsData);

    } catch (err) {
      if (err.isExtensionError) return;
      console.error('Error loading statistics:', err);
      setError('فشل تحميل الإحصائيات');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-state">جاري التحميل...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-container">
        <div className="alert alert-danger">
          <strong>خطأ:</strong> {error}
        </div>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="page-container">
        <div className="empty-state">
          <BarChart3 size={48} />
          <p>لا توجد إحصائيات متاحة</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      {/* Breadcrumb */}
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">←</span>
        <span className="active">الإحصائيات</span>
      </div>

      {/* Page Header */}
      <div className="page-header">
        <BarChart3 size={32} className="page-icon" />
        <div>
          <h1>الإحصائيات العامة</h1>
          <p>نظرة شاملة على بيانات النظام</p>
        </div>
      </div>

      {/* Overview Cards */}
      <div className="stats-grid">
        <div className="stat-card primary">
          <div className="stat-icon">
            <Database size={40} />
          </div>
          <div className="stat-content">
            <h3>عدد الجذور</h3>
            <div className="stat-number">{stats.totalRoots || 0}</div>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">
            <BarChart3 size={40} />
          </div>
          <div className="stat-content">
            <h3>عدد الأوزان</h3>
            <div className="stat-number">{stats.totalSchemes || 0}</div>
          </div>
        </div>

        <div className="stat-card info">
          <div className="stat-icon">
            <TrendingUp size={40} />
          </div>
          <div className="stat-content">
            <h3>الكلمات المولدة</h3>
            <div className="stat-number">{stats.totalDerivatives || 0}</div>
          </div>
        </div>

        <div className="stat-card warning">
          <div className="stat-icon">
            <Database size={40} />
          </div>
          <div className="stat-content">
            <h3>متوسط المشتقات</h3>
            <div className="stat-number">{stats.avgDerivatives ? stats.avgDerivatives.toFixed(2) : '0.00'}</div>
          </div>
        </div>
      </div>

      {/* Detailed Statistics - Roots */}
      <div className="card">
        <div className="card-header">
          <Database size={20} />
          <h3>إحصائيات الجذور</h3>
        </div>
        <div className="card-body">
          <div className="stats-details">
            <div className="stats-row">
              <span className="stats-label">إجمالي الجذور:</span>
              <span className="stats-value">{stats.totalRoots || 0}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">إجمالي المشتقات:</span>
              <span className="stats-value">{stats.totalDerivatives || 0}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">التكرار الكلي:</span>
              <span className="stats-value">{stats.totalFrequency || 0}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">متوسط المشتقات لكل جذر:</span>
              <span className="stats-value">
                {stats.avgDerivatives ? stats.avgDerivatives.toFixed(2) : '0.00'}
              </span>
            </div>
          </div>
        </div>
      </div>


      {/* Top Roots */}
      {stats.topRoots && Array.isArray(stats.topRoots) && stats.topRoots.length > 0 && (
        <div className="card">
          <div className="card-header">
            <TrendingUp size={20} />
            <h3>أكثر الجذور استخداماً</h3>
          </div>
          <div className="card-body">
            <div className="top-items-list">
              {stats.topRoots.map((item, index) => {
                const maxCount = stats.topRoots[0].count || 1;
                const percentage = (item.count / maxCount) * 100;
                
                return (
                  <div key={index} className="top-item">
                    <div className="top-rank">{index + 1}</div>
                    <div className="top-name">{item.root}</div>
                    <div className="top-bar">
                      <div 
                        className="top-bar-fill"
                        style={{ width: `${percentage}%` }}
                      ></div>
                    </div>
                    <div className="top-count">{item.count}</div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Statistics;