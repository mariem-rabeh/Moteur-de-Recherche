import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import RootManagement from './components/RootManagement';
import SchemeManagement from './components/SchemeManagement';
import Generation from './components/Generation';
import Validation from './components/Validation';
import Statistics from './components/Statistics';
import './styles/App.css';
import AdvancedSearch from './components/AdvancedSearch';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Navigate to="/roots" />} />
          <Route path="/roots" element={<RootManagement />} />
          <Route path="/schemes" element={<SchemeManagement />} />
          <Route path="/generation" element={<Generation />} />
          <Route path="/validation" element={<Validation />} />
          <Route path="/advanced" element={<AdvancedSearch />} />
          <Route path="/statistics" element={<Statistics />} />
          
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;