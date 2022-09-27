$(() => {
  var oEditors = [];
  nhn.husky.EZCreator.createInIFrame({
    oAppRef: oEditors,
    elPlaceHolder: "editor_text",
    sSkinURI: "../../../library/smarteditor2-2.8.2.3/SmartEditor2Skin.html",
    fCreator: "createSEditor2",
  });
});
