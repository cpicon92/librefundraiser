all:
	@echo Already built...
install:
	mkdir -p $(DESTDIR)/usr/share/librefundraiser
	cp -t $(DESTDIR)/usr/share/librefundraiser librefundraiser.jar librefundraiser.sh
	chmod +x $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh
	ln -sf $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh $(DESTDIR)/usr/bin/librefundraiser
	cp -t $(DESTDIR)/usr/share/pixmaps librefundraiser.png
	cp -t $(DESTDIR)/usr/share/applications librefundraiser.desktop
	xdg-mime install librefundraiser-lfd.xml
	xdg-icon-resource install --context mimetypes --size 48 librefundraiser-lfd_48.png application-x-librefundraiser
	xdg-icon-resource install --context mimetypes --size 256 librefundraiser-lfd_256.png application-x-librefundraiser
	xdg-mime default $(DESTDIR)/usr/share/applications/librefundraiser.desktop application/x-librefundraiser
