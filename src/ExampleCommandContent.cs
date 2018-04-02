using System;
using System.Diagnostics;
using System.IO;
using System.Linq;
using NationalInstruments;
using NationalInstruments.Composition;
using NationalInstruments.Controls.Shell;
using NationalInstruments.Core;
using NationalInstruments.ProjectExplorer.Design;
using NationalInstruments.Shell;
using NationalInstruments.SourceModel;
using NationalInstruments.SourceModel.Envoys;
using NationalInstruments.VI.SourceModel;

namespace ExamplePlugins.ExampleCommandPaneContent
{
    /// <summary>
    /// This export is a push command content provider.  Push command providers have the opportunity to add
    /// content to the command bars and toolbars for just about anything.  In this case we are going to add content to the
    /// command bar of the random number primitive.
    /// </summary>
    [ExportPushCommandContent]
    public class ExampleCommandContent : PushCommandContent
    {
        /// <summary>
        /// </summary>
        public static readonly ICommandEx LoadBetterCommmandCommand = new ShellRelayCommand(OnLoadBetter)
        {
            UniqueId = "ExamplePlugins.LoadBetterCommand",
            LabelTitle = "Load From Better File...",
        };

        /// <summary>
        /// </summary>
        public static readonly ICommandEx SaveBetterCommmandCommand = new ShellRelayCommand(OnSaveBetter)
        {
            UniqueId = "ExamplePlugins.SaveBetterCommand",
            LabelTitle = "Save To Better File...",
        };

        /// <summary>
        /// </summary>
        public static void OnLoadBetter(ICommandParameter parameter, ICompositionHost host, DocumentEditSite site)
        {
            var path = parameter.Parameter as string;
            Process loadBetter = new Process();
            loadBetter.StartInfo.UseShellExecute = false;
            loadBetter.StartInfo.CreateNoWindow = true;
            loadBetter.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            loadBetter.StartInfo.Arguments = path;
            loadBetter.StartInfo.FileName = LoadBetter.jar";
            loadBetter.Start();
            loadBetter.WaidForExit();
            /// NIMessageBox.Show(path);
        }

        /// <summary>
        /// </summary>
        public static void OnSaveBetter(ICommandParameter parameter, ICompositionHost host, DocumentEditSite site)
        {
            var path = parameter.Parameter as string;
            Process saveBetter = new Process();
            saveBetter.StartInfo.UseShellExecute = false;
            saveBetter.StartInfo.CreateNoWindow = true;
            saveBetter.StartInfo.WindowStyle = ProcessWindowStyle.Hidden;
            saveBetter.StartInfo.Arguments = path;
            saveBetter.StartInfo.FileName = "SaveBetter.jar";
            saveBetter.Start();
            saveBetter.WaitForExit();
            /// NIMessageBox.Show(path);
        }

        public override void CreateContextMenuContent(ICommandPresentationContext context, PlatformVisual sourceVisual)
        {
            var projectItem = sourceVisual.DataContext as ProjectItemViewModel;
            if (projectItem != null && projectItem.Envoy != null)
            {
                try
                {
                    var path = projectItem.Envoy?.QueryService<IReferencedFileService>().FirstOrDefault()?.StoragePath ?? string.Empty;
                    if (!string.IsNullOrEmpty(path))
                    {
                        context.Add(new ShellCommandInstance(LoadBetterCommmandCommand, path));
                        context.Add(new ShellCommandInstance(SaveBetterCommmandCommand, path));
                    }
                }
                catch (Exception)
                {
                }
            }
            base.CreateContextMenuContent(context, sourceVisual);
        }
    }
}
