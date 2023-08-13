public final class BasicHttpProcessor implements
    HttpProcessor, HttpRequestInterceptorList, HttpResponseInterceptorList, Cloneable {
    protected List requestInterceptors = null; 
    protected List responseInterceptors = null;
    public void addRequestInterceptor(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return;
        }
        if (this.requestInterceptors == null) {
            this.requestInterceptors = new ArrayList();
        }
        this.requestInterceptors.add(itcp);
    }
    public void addRequestInterceptor(final HttpRequestInterceptor itcp,
                                      int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        if (itcp == null) {
            return;
        }
        if (this.requestInterceptors == null) {
            if (index > 0) {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            this.requestInterceptors = new ArrayList();
        }
        this.requestInterceptors.add(index, itcp);
    }
    public void addResponseInterceptor(HttpResponseInterceptor itcp,
                                       int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        if (itcp == null) {
            return;
        }
        if (this.responseInterceptors == null) {
            if (index > 0) {
                throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            this.responseInterceptors = new ArrayList();
        }
        this.responseInterceptors.add(index, itcp);
    }
    public void removeRequestInterceptorByClass(final Class clazz) {
        if (this.requestInterceptors == null) {
            return;
        }
        for (Iterator it = this.requestInterceptors.iterator();
             it.hasNext(); ) {
            Object request = it.next();
            if (request.getClass().equals(clazz)) {
                it.remove();
            }
        }
    }
    public void removeResponseInterceptorByClass(final Class clazz) {
        if (this.responseInterceptors == null) {
            return;
        }
        for (Iterator it = this.responseInterceptors.iterator();
             it.hasNext(); ) {
            Object request = it.next();
            if (request.getClass().equals(clazz)) {
                it.remove();
            }
        }
    }
    public final
            void addInterceptor(final HttpRequestInterceptor interceptor) {
        addRequestInterceptor(interceptor);
    }
     public final
            void addInterceptor(final HttpRequestInterceptor interceptor,
                                int index) {
        addRequestInterceptor(interceptor, index);
    }
    public int getRequestInterceptorCount() {
        return (this.requestInterceptors == null) ?
            0 : this.requestInterceptors.size();
    }
    public HttpRequestInterceptor getRequestInterceptor(int index) {
        if ((this.requestInterceptors == null) ||
                (index < 0) || (index >= this.requestInterceptors.size()))
            return null;
        return (HttpRequestInterceptor) this.requestInterceptors.get(index);
    }
    public void clearRequestInterceptors() {
        this.requestInterceptors = null;
    }
    public void addResponseInterceptor(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return;
        }
        if (this.responseInterceptors == null) {
            this.responseInterceptors = new ArrayList();
        }
        this.responseInterceptors.add(itcp);
    }
    public final
            void addInterceptor(final HttpResponseInterceptor interceptor) {
        addResponseInterceptor(interceptor);
    }
    public final void addInterceptor(final HttpResponseInterceptor interceptor,
                                     int index) {
        addResponseInterceptor(interceptor, index);
    }
    public int getResponseInterceptorCount() {
        return (this.responseInterceptors == null) ?
            0 : this.responseInterceptors.size();
    }
    public HttpResponseInterceptor getResponseInterceptor(int index) {
        if ((this.responseInterceptors == null) ||
                (index < 0) || (index >= this.responseInterceptors.size()))
            return null;
        return (HttpResponseInterceptor) this.responseInterceptors.get(index);
    }
    public void clearResponseInterceptors() {
        this.responseInterceptors = null;
    }
    public void setInterceptors(final List list) {
        if (list == null) {
            throw new IllegalArgumentException("List must not be null.");
        }
        if (this.requestInterceptors != null) {
            this.requestInterceptors.clear();
        }
        if (this.responseInterceptors != null) {
            this.responseInterceptors.clear();
        }
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj instanceof HttpRequestInterceptor) {
                addInterceptor((HttpRequestInterceptor)obj);
            }
            if (obj instanceof HttpResponseInterceptor) {
                addInterceptor((HttpResponseInterceptor)obj);
            }
        }
    }
    public void clearInterceptors() {
        clearRequestInterceptors();
        clearResponseInterceptors();
    }
    public void process(
            final HttpRequest request,
            final HttpContext context)
            throws IOException, HttpException {
        if (this.requestInterceptors != null) {
            for (int i = 0; i < this.requestInterceptors.size(); i++) {
                HttpRequestInterceptor interceptor =
                    (HttpRequestInterceptor) this.requestInterceptors.get(i);
                interceptor.process(request, context);
            }
        }
    }
    public void process(
            final HttpResponse response,
            final HttpContext context)
            throws IOException, HttpException {
        if (this.responseInterceptors != null) {
            for (int i = 0; i < this.responseInterceptors.size(); i++) {
                HttpResponseInterceptor interceptor =
                    (HttpResponseInterceptor) this.responseInterceptors.get(i);
                interceptor.process(response, context);
            }
        }
    }
    protected void copyInterceptors(final BasicHttpProcessor target) {
        if (this.requestInterceptors != null) {
            target.requestInterceptors =
                new ArrayList(this.requestInterceptors);
        }
        if (this.responseInterceptors != null) {
            target.responseInterceptors =
                new ArrayList(this.responseInterceptors);
        }
    }
    public BasicHttpProcessor copy() {
        BasicHttpProcessor clone = new BasicHttpProcessor();
        copyInterceptors(clone);
        return clone;
    }
    public Object clone() throws CloneNotSupportedException {
        BasicHttpProcessor clone = (BasicHttpProcessor) super.clone();
        copyInterceptors(clone);
        return clone;
    }
}
