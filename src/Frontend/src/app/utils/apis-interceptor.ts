import { HttpInterceptorFn } from '@angular/common/http';

export const apisInterceptor: HttpInterceptorFn = (req, next) => {
  const localData = localStorage.getItem('loginData');
  if (localData) {
    const token = JSON.parse(localData).token;
    if (token) {
      const newReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
      return next(newReq);
    }
  }
  return next(req);
};
