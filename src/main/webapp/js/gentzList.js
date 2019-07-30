$(document).ready(function() {
	//to be dyanmised later! Consult XML for it!
	var ger = {
		processing: "Ausgabe der Suchergebnisse&nbsp;...",
		search: "Suchergebnisse einschr&auml;nken:",
		lengthMenu: "Anzeige von _MENU_ Eintr&auml;gen",
        info: "Anzeige von Eintrag _START_ bis _END_ von _TOTAL_ Eintr&auml;gen",
        infoEmpty: "Zeige Eintr&auml;ge 0 bis 0 aus 0 Eintr&auml;gen",
        infoFiltered: "(eingeschr&auml;nkt aus insgesamt _MAX_ Eintr&auml;gen)",
        infoPostFix: "",
        loadingRecords: "Einladen der Eintr&auml;ge&nbsp;...",
        zeroRecords: "Keine Eintr&auml;ge vorhanden",
        emptyTable:	"Keine Daten f&uuml;r Tabelle verf&uuml;gbar",
        paginate: {
			first:      "Erste",
			previous:   "Vorherige",
			next:       "N&auml;chste",
			last:       "Letzte"
        },
        aria: {
			sortAscending:  ": klicken, um in aufsteigender Reihenfolge zu sortieren",
			sortDescending: ": klicken, um in absteigender Reihenfolge zu sortieren"
        }
	}
	var fra = {
		processing: "Traitement en cours...",
		search: "Limiter r&eacute;sultats :",
		lengthMenu: "Afficher _MENU_ &eacute;l&eacute;ments",
        info: "Affichage de l'&eacute;lement _START_ &agrave; _END_ sur _TOTAL_ &eacute;l&eacute;ments",
        infoEmpty: "Affichage de l'&eacute;lement 0 &agrave; 0 sur 0 &eacute;l&eacute;ments",
        infoFiltered: "(filtr&eacute; de _MAX_ &eacute;l&eacute;ments au total)",
        infoPostFix: "",
        loadingRecords: "Chargement en cours...",
        zeroRecords: "Aucun &eacute;l&eacute;ment &agrave; afficher",
        emptyTable:	"Aucune donnée disponible dans le tableau",
        paginate: {
			first:      "Premier",
			previous:   "Pr&eacute;c&eacute;dent",
			next:       "Suivant",
			last:       "Dernier"
        },
        aria: {
			sortAscending:  ": activer pour trier la colonne par ordre croissant",
			sortDescending: ": activer pour trier la colonne par ordre décroissant"
        }
	}
	var selLang = ger;
	
	$('.dataTable').DataTable({
		language: selLang,
		fixedColumns: true
		/*	language: {
			url: '/localisation/fr_FR.json'
		}	*/
	});
	
	$('#letterListTable').on('click','tbody tr',function(){
		window.location.href = "/KtM/letter/"+$(this).attr("id");
	});
	
});