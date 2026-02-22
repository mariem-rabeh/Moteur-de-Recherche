import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Créer une instance Axios avec configuration
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
});

// Intercepteur pour logger les requêtes (debug)
axiosInstance.interceptors.request.use(
  (config) => {
    console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`);
    return config;
  },
  (error) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

// Intercepteur pour gérer les erreurs globalement
axiosInstance.interceptors.response.use(
  (response) => {
    console.log(`[API Response] ${response.config.url}`, response.data);
    return response;
  },
  (error) => {
    // Ignorer les erreurs d'extensions
    if (error.config && error.config.url && 
        (error.config.url.includes('chrome-extension') || 
         error.config.url.includes('site_integration'))) {
      console.warn('[Extension Error] Ignored:', error.message);
      return Promise.reject({ isExtensionError: true, ...error });
    }

    // Gérer les erreurs réseau
    if (!error.response) {
      console.error('[Network Error]', error.message);
      return Promise.reject({
        message: 'خطأ في الاتصال بالخادم. تحقق من تشغيل الـ Backend',
        isNetworkError: true
      });
    }

    // Gérer les erreurs HTTP
    const status = error.response.status;
    let message = 'حدث خطأ غير متوقع';

    switch (status) {
      case 400:
        message = 'بيانات غير صحيحة';
        break;
      case 404:
        message = 'المورد غير موجود';
        break;
      case 500:
        message = 'خطأ في الخادم';
        break;
    }

    console.error(`[API Error ${status}]`, error.response.data);
    return Promise.reject({
      message,
      status,
      data: error.response.data
    });
  }
);

const api = {
  // ==================== ROOTS ====================
  getRoots: (search = '', page = 1, limit = 10) =>
    axiosInstance.get('/roots', { params: { search, page, limit } }),
  
  addRoot: (root) =>
    axiosInstance.post('/roots', { root }),
  
  deleteRoot: (root) =>
    axiosInstance.delete(`/roots/${encodeURIComponent(root)}`),
  
  uploadRoots: (formData) =>
    axiosInstance.post('/roots/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // ==================== SCHEMES ====================
  getSchemes: () =>
    axiosInstance.get('/schemes'),
  
  addScheme: (name, rule) =>
    axiosInstance.post('/schemes', { name, rule }),
  
  updateScheme: (name, rule) =>
    axiosInstance.put(`/schemes/${encodeURIComponent(name)}`, { rule }),
  
  deleteScheme: (name) =>
    axiosInstance.delete(`/schemes/${encodeURIComponent(name)}`),
  
  uploadSchemes: (formData) =>
    axiosInstance.post('/schemes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),

  // ==================== GENERATION ====================
  generateWord: (root, scheme) =>
    axiosInstance.post('/generate/word', { root, scheme }),
  
  generateFamily: (root) =>
    axiosInstance.post('/generate/family', { root }),

  // ==================== VALIDATION ====================
  validateWord: (word, root) =>
    axiosInstance.post('/validate/check', { word, root }),
  
  decomposeWord: (word) =>
    axiosInstance.post('/validate/decompose', { word }),
  
  findAllRoots: (word) =>
    axiosInstance.post('/validate/find-roots', { word }),

  // ==================== STATISTICS ====================
  getStatistics: () =>
    axiosInstance.get('/statistics'),
  
  searchByScheme: (scheme) =>
    axiosInstance.post('/search/by-scheme', { scheme }),

  getDerivatives: (root) =>
    axiosInstance.get(`/search/roots/${encodeURIComponent(root)}/derivatives`),
  generateByScheme: (scheme) =>
    axiosInstance.post('/generate/by-scheme', { scheme }),
};

export default api;