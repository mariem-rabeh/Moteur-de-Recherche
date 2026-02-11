import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { 
  BookOpen, 
  FileText, 
  Wand2, 
  CheckCircle, 
  BarChart3,
  Search,
  Download,
  Upload
} from 'lucide-react';

const Sidebar = () => {
  const location = useLocation();

  const menuItems = [
    { 
      path: '/roots', 
      icon: <BookOpen size={20} />, 
      label: 'إدارة الجذور',
      subItems: [
        { label: 'تحميل الجذور', icon: <Upload size={16} /> },
        { label: 'البحث عن جذور', icon: <Search size={16} /> },
        { label: 'حفظ جذور', icon: <Download size={16} /> }
      ]
    },
    { 
      path: '/schemes', 
      icon: <FileText size={20} />, 
      label: 'إدارة الأوزان الصرفية',
      subItems: [
        { label: 'إضافة وزن', icon: <Upload size={16} /> },
        { label: 'تعديل وزن', icon: <Search size={16} /> },
        { label: 'حفظ الوزان', icon: <Download size={16} /> }
      ]
    },
    { 
      path: '/generation', 
      icon: <Wand2 size={20} />, 
      label: 'التوليد الصرفي',
      subItems: [
        { label: 'توليد كلمة', icon: <Wand2 size={16} /> },
        { label: 'توليد عائلة صرفية', icon: <FileText size={16} /> }
      ]
    },
    { 
      path: '/validation', 
      icon: <CheckCircle size={20} />, 
      label: 'الصحة الصرفية',
      subItems: [
        { label: 'وصف منكمل', icon: <FileText size={16} /> }
      ]
    },
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
            
            {item.subItems && location.pathname === item.path && (
              <div className="sub-items">
                {item.subItems.map((subItem, index) => (
                  <div key={index} className="sub-item">
                    <span className="sub-icon">{subItem.icon}</span>
                    <span className="sub-label">{subItem.label}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </nav>
    </aside>
  );
};

export default Sidebar;