import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const currentUser = this.authService.currentUserValue;
    const correlationId = 'ui-' + Math.random().toString(36).substring(2, 10);

    let headers = request.headers
      .set('X-Correlation-Id', correlationId);

    if (currentUser && currentUser.token) {
      headers = headers
        .set('Authorization', `Bearer ${currentUser.token}`)
        .set('X-Company-Id', currentUser.companyId)
        .set('X-Legal-Entity-Id', currentUser.legalEntityId)
        .set('X-User-Id', currentUser.id)
        .set('X-User-Roles', currentUser.role);
    }

    const modifiedReq = request.clone({ headers });
    return next.handle(modifiedReq);
  }
}
