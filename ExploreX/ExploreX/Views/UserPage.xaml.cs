using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using Xamarin.Forms;
using Xamarin.Forms.Xaml;

namespace ExploreX.Views
{
    [XamlCompilation(XamlCompilationOptions.Compile)]
    public partial class UserPage : ContentPage
    {
        public UserPage()
        {
            InitializeComponent();
        }

        private void Button_Clicked(object sender, EventArgs e)
        {
            if(email.Text=="admin" && password.Text=="123")
            {
                DisplayAlert("Sukces", "Logowanie powiodło się.", "Kontynuuj");
            }
            else
            {
                DisplayAlert("Błąd logowania", "Logowanie nie powiodło się.", "Spróbuj ponownie");
            }
        }

        private void TapGestureRecognizer_Tapped(object sender, EventArgs e)
        {
            Navigation.PushAsync(new RegisterPage());
        }
    }
}