import React from 'react';
import Sidebar from './Sidebar';

const Layout = ({ children }) => {
  return (
    <div className="app-container">
      {/* Ornement supérieur */}
      <div className="ornament-top"></div>
      
      {/* En-tête */}
      <header className="app-header">
        <div className="header-ornament-left"></div>
        <h1 className="app-title">نِظَامُ المُعَالَجَةِ الصَّرْفِيَّةِ لِلْجُذُورِ العَرَبِيَّة</h1>
        <div className="header-ornament-right"></div>
      </header>

      <div className="app-content">
        <Sidebar />
        <main className="main-content">
          <div className="content-wrapper">
            {children}
          </div>
        </main>
      </div>

      {/* Ornement inférieur */}
      <div className="ornament-bottom"></div>
    </div>
  );
};

export default Layout;