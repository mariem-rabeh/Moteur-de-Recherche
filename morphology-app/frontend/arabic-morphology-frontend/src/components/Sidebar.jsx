import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { 
  BookOpen, 
  FileText, 
  Wand2, 
  CheckCircle, 
  Search        ,
  BarChart3
} from 'lucide-react';

const Sidebar = () => {
  const location = useLocation();

  const menuItems = [
    { 
      path: '/roots', 
      icon: <BookOpen size={20} />, 
      label: 'إدارة الجذور'
    },
    { 
      path: '/schemes', 
      icon: <FileText size={20} />, 
      label: 'إدارة الأوزان الصرفية'
    },
    { 
      path: '/generation', 
      icon: <Wand2 size={20} />, 
      label: 'التوليد الصرفي'
    },
    { 
      path: '/validation', 
      icon: <CheckCircle size={20} />, 
      label: 'الصحة الصرفية'
    },
    { path: '/advanced',
      icon: <Search size={20} />, 
      label: 'البحث المتقدم' },
    { 
      path: '/statistics', 
      icon: <BarChart3 size={20} />, 
      label: 'الإحصائيات'
    }
    
  ];

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="logo-container">
          <BookOpen size={40} className="logo-icon" />
        </div>
        <h2 className="sidebar-title">نِظَامُ الجُذُورِ العَرَبِيَّة</h2>
        <p className="sidebar-subtitle">لِلْجُذُورِ العَرَبِيَّة</p>
      </div>

      <nav className="sidebar-nav">
        {menuItems.map((item) => (
          <div key={item.path} className="nav-item-container">
            <Link
              to={item.path}
              className={`nav-item ${location.pathname === item.path ? 'active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              <span className="nav-label">{item.label}</span>
            </Link>
          </div>
        ))}
      </nav>
    </aside>
  );
};

export default Sidebar;