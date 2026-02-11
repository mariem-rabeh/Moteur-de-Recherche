import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = {
  // ==================== ROOTS ====================
  getRoots: (search = '', page = 1, limit = 10) => 
    axios.get(`${API_BASE_URL}/roots`, { params: { search, page, limit } }),
  
  addRoot: (root) => 
    axios.post(`${API_BASE_URL}/roots`, { root }),
  
  deleteRoot: (root) => 
    axios.delete(`${API_BASE_URL}/roots/${encodeURIComponent(root)}`),
  
  uploadRoots: (formData) => 
    axios.post(`${API_BASE_URL}/roots/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // ==================== SCHEMES ====================
  getSchemes: () => 
    axios.get(`${API_BASE_URL}/schemes`),
  
  addScheme: (name, rule) => 
    axios.post(`${API_BASE_URL}/schemes`, { name, rule }),
  
  updateScheme: (name, rule) => 
    axios.put(`${API_BASE_URL}/schemes/${encodeURIComponent(name)}`, { rule }),
  
  deleteScheme: (name) => 
    axios.delete(`${API_BASE_URL}/schemes/${encodeURIComponent(name)}`),
  
  uploadSchemes: (formData) => 
    axios.post(`${API_BASE_URL}/schemes/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // ==================== GENERATION ====================
  generateWord: (root, scheme) => 
    axios.post(`${API_BASE_URL}/generate/word`, { root, scheme }),
  
  generateFamily: (root) => 
    axios.post(`${API_BASE_URL}/generate/family`, { root }),

  // ==================== VALIDATION ====================
  validateWord: (word, root) => 
    axios.post(`${API_BASE_URL}/validate/check`, { word, root }),
  
  decomposeWord: (word) => 
    axios.post(`${API_BASE_URL}/validate/decompose`, { word }),
  
  findAllRoots: (word) => 
    axios.post(`${API_BASE_URL}/validate/find-roots`, { word }),

  // ==================== STATISTICS ====================
  getStatistics: () => 
    axios.get(`${API_BASE_URL}/statistics`)
};

export default api;