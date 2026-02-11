import React, { useState, useEffect } from 'react';
import { BarChart3, TrendingUp, Database } from 'lucide-react';
import api from '../services/api';

const Statistics = () => {
  const [stats, setStats] = useState(null);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      const response = await api.getStatistics();
      setStats(response.data);
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  if (!stats) {
    return <div className="loading">جاري التحميل...</div>;
  }

  return (
    <div className="page-container">
      <div className="breadcrumb">
        <span>الرئيسية</span>
        <span className="separator">›</span>
        <span>الإحصائيات</span>
      </div>

      <div className="page-header">
        <div className="ornament-icon">
          <BarChart3 size={32} />
        </div>
        <h2 className="page-title">الإحصائيات العامة</h2>
      </div>

      {/* Overview Cards */}
      <div className="stats-grid">
        <div className="stat-card primary">
          <div className="stat-icon">
            <Database size={40} />
          </div>
          <div className="stat-content">
            <h3>عدد الجذور</h3>
            <div className="stat-number">{stats.totalRoots}</div>
          </div>
        </div>

        <div className="stat-card success">
          <div className="stat-icon">
            <BarChart3 size={40} />
          </div>
          <div className="stat-content">
            <h3>عدد الأوزان</h3>
            <div className="stat-number">{stats.totalSchemes}</div>
          </div>
        </div>

        <div className="stat-card info">
          <div className="stat-icon">
            <TrendingUp size={40} />
          </div>
          <div className="stat-content">
            <h3>الكلمات المولدة</h3>
            <div className="stat-number">{stats.totalGenerated}</div>
          </div>
        </div>

        <div className="stat-card warning">
          <div className="stat-icon">
            <Database size={40} />
          </div>
          <div className="stat-content">
            <h3>متوسط المشتقات</h3>
            <div className="stat-number">{stats.avgDerivatives}</div>
          </div>
        </div>
      </div>

      {/* Detailed Statistics */}
      <div className="card">
        <div className="card-header">
          <BarChart3 size={20} />
          <h3>إحصائيات الجذور</h3>
        </div>
        <div className="card-body">
          <div className="stats-details">
            <div className="stats-row">
              <span className="stats-label">إجمالي الجذور:</span>
              <span className="stats-value">{stats.totalRoots}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">إجمالي المشتقات:</span>
              <span className="stats-value">{stats.totalDerivatives}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">التكرار الكلي:</span>
              <span className="stats-value">{stats.totalFrequency}</span>
            </div>
            <div className="stats-row">
              <span className="stats-label">متوسط المشتقات لكل جذر:</span>
              <span className="stats-value">{stats.avgDerivatives}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <TrendingUp size={20} />
          <h3>إحصائيات الأوزان</h3>
        </div>
        <div className="card-body">
          <div className="stats-details">
            <div className="stats-row">
              <span className="stats-label">إجمالي الأوزان:</span>
              <span className="stats-value">{stats.totalSchemes}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Top Roots */}
      {stats.topRoots && stats.topRoots.length > 0 && (
        <div className="card">
          <div className="card-header">
            <TrendingUp size={20} />
            <h3>أكثر الجذور استخداماً</h3>
          </div>
          <div className="card-body">
            <div className="top-items-list">
              {stats.topRoots.map((item, index) => (
                <div key={index} className="top-item">
                  <div className="top-rank">{index + 1}</div>
                  <div className="top-name">{item.root}</div>
                  <div className="top-bar">
                    <div 
                      className="top-bar-fill"
                      style={{ width: `${(item.count / stats.topRoots[0].count) * 100}%` }}
                    ></div>
                  </div>
                  <div className="top-count">{item.count}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Statistics;