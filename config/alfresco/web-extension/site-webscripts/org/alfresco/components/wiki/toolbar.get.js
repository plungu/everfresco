function main()
{
   var title = page.url.args.title;
   model.exists = false;
   if (title)
   {
      var context = page.url.context + "/page/site/" + page.url.templateArgs.site + "/wiki-page?title=" + page.url.args.title,
      uri = "/slingshot/wiki/page/" + encodeURIComponent(page.url.templateArgs.site) + "/" + encodeURIComponent(page.url.args.title) + "?context=" + escape(context),
      connector = remote.connect("alfresco"),
      result = connector.get(uri);

      // we allow 200 and 404 as valid responses - any other error then cannot show page
      // the 404 response means we can create a new page for the title
      if (result.status.code == status.STATUS_OK || result.status.code == status.STATUS_NOT_FOUND)
      {
         model.exists = (result.status.code == status.STATUS_OK);
      }
   }
}

main();