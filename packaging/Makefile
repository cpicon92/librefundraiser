all:
	@echo Already built...
install:
	mkdir -p $(DESTDIR)/usr/share/librefundraiser
	cp -t $(DESTDIR)/usr/share/librefundraiser librefundraiser.jar librefundraiser.sh
	chmod +x $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh
	ln -sf $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh $(DESTDIR)/usr/bin/librefundraiser
	cp -t $(DESTDIR)/usr/share/pixmaps librefundraiser.png
	cp -t $(DESTDIR)/usr/share/applications librefundraiser.desktop
	cp -t $(DESTDIR)/usr/share/mime/application x-librefundraiser.xml
	cp -t $(DESTDIR)/usr/share/mime/application x-librefundraiser.xml
	cp librefundraiser-lfd_48.png $(DESTDIR)/usr/share/icons/hicolor/48x48/mimetypes/application-x-librefundraiser.png
	cp librefundraiser-lfd_256.png $(DESTDIR)/usr/share/icons/hicolor/256x256/mimetypes/application-x-librefundraiser.png
	cp librefundraiser-lfd.svg $(DESTDIR)/usr/share/icons/hicolor/scalable/mimetypes/application-x-librefundraiser.svg
