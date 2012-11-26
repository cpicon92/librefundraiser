all:
	@echo Already built...
install:
	mkdir -p $(DESTDIR)/usr/share/librefundraiser
	cp -t $(DESTDIR)/usr/share/librefundraiser librefundraiser.jar librefundraiser.sh
	chmod +x $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh
	ln -sf $(DESTDIR)/usr/share/librefundraiser/librefundraiser.sh $(DESTDIR)/usr/bin/librefundraiser
	cp -t $(DESTDIR)/usr/share/pixmaps librefundraiser.png
	cp -t $(DESTDIR)/usr/share/applications librefundraiser.desktop
